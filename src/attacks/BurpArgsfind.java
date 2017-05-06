package attacks;

import burp.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by beched on 06.05.17.
 */
public class BurpArgsfind {
    private IExtensionHelpers iHelpers;
    private IBurpExtenderCallbacks iCallbacks;
    private byte[] iRequest;
    private IHttpService iService;
    private int iSize;
    private short iCode;
    private List<String> iBase = new ArrayList<String>();
    private List<String> iFound = new ArrayList<String>();
    private byte pType;
    private IRequestInfo iInfo;
    private java.net.URL iURL;

    public BurpArgsfind(IBurpExtenderCallbacks callbacks) {
        iCallbacks = callbacks;
        iHelpers = callbacks.getHelpers();
        InputStream is = getClass().getResourceAsStream("/argsbase.txt");
        try {
            final Scanner s = new Scanner(is);
            while (s.hasNextLine()) {
                final String line = s.nextLine();
                iBase.add(line);
            }
            is.close();
        } catch (IOException ex) {
            iCallbacks.printError("WTF?");
        }
    }

    public void args_dichotomy(int l, int r) {
        String query = "";
        byte[] Request = new byte[1];
        for(int i = l; i < r; ++i) {
            query += iBase.get(i) + "=1";
            if(pType == IParameter.PARAM_COOKIE) {
                query += ";";
            }
            else if(pType == IParameter.PARAM_BODY || pType == IParameter.PARAM_URL) {
                query += "&";
            }
        }
        if(pType == IParameter.PARAM_URL) {
            try {
                java.net.URL URL = new java.net.URL(iURL + "?" + query);
                Request = iHelpers.buildHttpRequest(URL);
            } catch (Exception ex) {
                iCallbacks.printError("WTF??");
            }
        }
        if(pType == IParameter.PARAM_COOKIE) {
            List<String> headers = iInfo.getHeaders();
            headers.add("Cookie: " + query);
            Request = iHelpers.buildHttpMessage(headers, iHelpers.stringToBytes(query));
        }
        if(pType == IParameter.PARAM_BODY) {
            Request = iHelpers.buildHttpMessage(iInfo.getHeaders(), iHelpers.stringToBytes(query));
            /*byte[] query_string = iHelpers.stringToBytes(query);
            byte[] hdr = iHelpers.stringToBytes("Content-Length: " + query_string.length + "\r\n\r\n");
            Request = new byte[iRequest.length - 4 + hdr.length + query_string.length];
            System.arraycopy(iRequest, 0, Request, 0, iRequest.length - 4);
            System.arraycopy(hdr, 0, Request, iRequest.length - 4, hdr.length);
            System.arraycopy(query, 0, Request, iRequest.length - 4 + hdr.length, query_string.length);*/
        }
        int mid = (l + r) / 2;
        /*
        // This approach is painfully CPU-consuming
        IParameter param;
        for(int i = l; i < r; ++i) {
            param = iHelpers.buildParameter(iBase.get(i), "1", pType);
            Request = iHelpers.addParameter(Request, param);
        }
        */
        byte[] resp = iCallbacks.makeHttpRequest(iService, Request).getResponse();
        IResponseInfo info = iHelpers.analyzeResponse(resp);
        int size = resp.length;
        short code = info.getStatusCode();
        if(code == 414 || (pType == IParameter.PARAM_COOKIE && code == 400)) {
            iCallbacks.printOutput("\tToo big base, splitting...");
            args_dichotomy(l, mid);
            args_dichotomy(mid, r);
            return;
        }
        if(code != iCode || size != iSize) {
            iCallbacks.printOutput("\t*");
            //iCallbacks.printOutput(iHelpers.bytesToString(resp));
            if(r - l == 1) {
                iFound.add(iBase.get(l));
                iCallbacks.printOutput("\t[FOUND] " + iBase.get(l));
            }
            else {
                args_dichotomy(l, mid);
                args_dichotomy(mid, r);
            }
        }
    }

    public void process(IHttpRequestResponse Request) {
        iRequest = Request.getRequest();
        iService = Request.getHttpService();
        iInfo =  iHelpers.analyzeRequest(Request);
        iURL = iInfo.getUrl();
        /*java.util.List<IParameter> params = info.getParameters();

        // Now clean the parameters

        for(IParameter param: params) {
            iRequest = IExtensionHelpers.removeParameter(Request, param);
        }*/
        iCallbacks.printOutput("[START] " + iURL);
        iRequest = iHelpers.buildHttpRequest(iURL);
        iCallbacks.makeHttpRequest(iService, iRequest);
        byte[] resp = iCallbacks.makeHttpRequest(iService, iRequest).getResponse();
        IResponseInfo resp_info = iHelpers.analyzeResponse(resp);
        iSize = resp.length;
        iCode = resp_info.getStatusCode();

        iCallbacks.printOutput("Searching for GET-parameters");
        pType = IParameter.PARAM_URL;
        args_dichotomy(0, iBase.size());
        iCallbacks.printOutput("Finished");

        iCallbacks.printOutput("Searching for Cookie-parameters");
        pType = IParameter.PARAM_COOKIE;
        args_dichotomy(0, iBase.size());
        iCallbacks.printOutput("Finished");

        iCallbacks.printOutput("Searching for POST-parameters");
        pType = IParameter.PARAM_BODY;
        iRequest = iHelpers.toggleRequestMethod(iRequest);
        args_dichotomy(0, iBase.size());
        iCallbacks.printOutput("Finished");

        /*iCallbacks.printOutput("Searching for JSON-parameters");
        pType = IParameter.PARAM_JSON;
        args_dichotomy(0, iBase.size());

        iCallbacks.printOutput("Searching for XML-parameters");
        pType = IParameter.PARAM_XML;
        args_dichotomy(0, iBase.size());*/
    }
}

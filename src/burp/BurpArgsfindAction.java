package burp;

import attacks.BurpArgsfind;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by beched on 06.05.17.
 */
class BurpArgsfindAction implements ActionListener, ItemListener {
    private IContextMenuInvocation invocation;
    private IBurpExtenderCallbacks iCallbacks;
    private BurpArgsfind argsfind;

    public BurpArgsfindAction(final IBurpExtenderCallbacks callbacks, IContextMenuInvocation invocation, BurpArgsfind argsfind) {
        this.iCallbacks = callbacks;
        this.invocation = invocation;
        this.argsfind = argsfind;
    }

    public void actionPerformed(ActionEvent e) {
        IHttpRequestResponse[] selectedMessages = invocation.getSelectedMessages();
        for (IHttpRequestResponse iReqResp : selectedMessages) {
            Thread queryThread = new Thread() {
                public void run() {
                    try {
                        argsfind.process(iReqResp);
                    } catch (Exception ex) {

                    }
                }
            };
            queryThread.start();
        }
    }

    public void itemStateChanged(ItemEvent e) {
    }
}
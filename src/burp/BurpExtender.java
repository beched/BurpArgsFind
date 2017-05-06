/**
 * Created by beched on 06.05.17.
 */
package burp;

import attacks.BurpArgsfind;

import java.io.PrintStream;

public class BurpExtender implements IBurpExtender
{
    public void registerExtenderCallbacks (IBurpExtenderCallbacks callbacks)
    {
        BurpArgsfind argsfind = new BurpArgsfind(callbacks);
        callbacks.registerContextMenuFactory(new BurpArgsfindMenu(callbacks, argsfind));
    }
}
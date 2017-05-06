package burp;

import attacks.BurpArgsfind;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenuItem;

/**
 * Created by beched on 06.05.17.
 */
public class BurpArgsfindMenu implements IContextMenuFactory {
    private BurpArgsfind argsfind;
    private IBurpExtenderCallbacks iCallbacks;

    public BurpArgsfindMenu(IBurpExtenderCallbacks callbacks, BurpArgsfind argsfind) {
        this.argsfind = argsfind;
        this.iCallbacks = callbacks;
    }

    @Override
    public List<JMenuItem> createMenuItems(final IContextMenuInvocation invocation) {
        JMenuItem sendToArgsFindMenu = new JMenuItem("Send to ArgsFind");

        sendToArgsFindMenu.addActionListener(new BurpArgsfindAction(iCallbacks, invocation, argsfind));

        List<JMenuItem> menus = new ArrayList();
        menus.add(sendToArgsFindMenu);

        return menus;
    }
}
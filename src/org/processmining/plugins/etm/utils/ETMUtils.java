package org.processmining.plugins.etm.utils;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class ETMUtils {

	/**
	 * Puts the provided string on the clipboard for easy paste
	 * @param message
	 */
	public static void onClipboard(String message){
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = new StringSelection(message);
		clipboard.setContents(transferable, null);
	}
	
}

/*
 * Copyright 2009, 2010
 *    Niklas Kyster Rasmussen
 *    Flaming Candle*
 *
 *  (*) Eve-Online names @ http://www.eveonline.com/
 *
 * This file is part of jEveAssets.
 *
 * jEveAssets is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * jEveAssets is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jEveAssets; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package net.nikr.eve.jeveasset.gui.shared;

import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;


public class JNumberField extends JTextField implements FocusListener{

	private String defaultValue;

	public JNumberField() {
		this("0");
	}

	public JNumberField(String defaultValue) {
		this.defaultValue = defaultValue;
		this.addFocusListener(this);
		this.setDocument( new NumberPlainDocument() );
	}

	@Override
	public void focusGained(FocusEvent e) {

	}

	@Override
	public void focusLost(FocusEvent e) {
		if (super.getText().equals("")){
			super.setText(defaultValue);
		}
	}

	@Override
	public String getText() {
		if (super.getText().equals("")) super.setText(defaultValue);
		return super.getText();
	}

	public class NumberPlainDocument extends PlainDocument {

		@Override
		public void insertString(int offset, String string, AttributeSet attributes) throws BadLocationException {
			int length = getLength();
			if (string == null) {
				return;
			}
			String newValue;
			if (length == 0) {
				newValue = string;
			} else {
				String currentContent = getText(0, length);
				StringBuffer currentBuffer = new StringBuffer(currentContent);
				currentBuffer.insert(offset, string);
				newValue = currentBuffer.toString();
			}
			try {
				Integer.parseInt(newValue);
				super.insertString(offset, string, attributes);
			} catch (NumberFormatException exception) {
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}
}

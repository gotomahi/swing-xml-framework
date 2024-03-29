package com.framework.swing.table.cell;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.EventObject;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

import com.framework.reflect.ReflectionInvoker;
import com.framework.swing.components.DatePicker;
import com.framework.swing.components.TableModel;

/**
 * The default editor for table and tree cells.
 * <p>
 * <strong>Warning:</strong> Serialized objects of this class will not be
 * compatible with future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running the
 * same version of Swing. As of 1.4, support for long term storage of all
 * JavaBeans<sup><font size="-2">TM</font></sup> has been added to the
 * <code>java.beans</code> package. Please see {@link java.beans.XMLEncoder}.
 * 
 * @version 1.57 05/05/07
 * @author Alan Chung
 * @author Philip Milne
 */

public class DefaultCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {

	//
	// Instance Variables
	//

	/** The Swing component being edited. */
	protected JComponent editorComponent;
	/**
	 * The delegate class which handles all methods sent from the
	 * <code>CellEditor</code>.
	 */
	protected EditorDelegate delegate;
	/**
	 * An integer specifying the number of clicks needed to start editing. Even
	 * if <code>clickCountToStart</code> is defined as zero, it will not
	 * initiate until a click occurs.
	 */
	protected int clickCountToStart = 1;
	protected String rendererData;
	protected boolean dynamic;

	/**
	 * Constructs a <code>DefaultCellEditor</code> that uses a text field.
	 * 
	 * @param textField
	 *            a <code>JTextField</code> object
	 */
	@ConstructorProperties({ "component" })
	public DefaultCellEditor(final JTextField textField) {
		editorComponent = textField;
		this.clickCountToStart = 2;
		delegate = new EditorDelegate() {
			public void setValue(Object value) {
				textField.setText((value != null) ? value.toString() : "");
			}

			public Object getCellEditorValue() {
				return textField.getText();
			}
		};
		textField.addActionListener(delegate);
	}

	/**
	 * Constructs a <code>DefaultCellEditor</code> object that uses a check box.
	 * 
	 * @param checkBox
	 *            a <code>JCheckBox</code> object
	 */
	public DefaultCellEditor(final JCheckBox checkBox) {
		editorComponent = checkBox;

	}

	public DefaultCellEditor(final TableCellPanel panel) {
		editorComponent = panel;

	}

	public DefaultCellEditor(final DatePicker datePicker) {
		editorComponent = datePicker;

	}

	public DefaultCellEditor(final JComboBox comboBox, String rendererData, boolean dynamic) {
		this.editorComponent = comboBox;
		this.rendererData = rendererData;
		this.dynamic = dynamic;
	}

	/**
	 * Constructs a <code>DefaultCellEditor</code> object that uses a combo box.
	 * 
	 * @param comboBox
	 *            a <code>JComboBox</code> object
	 */
	public DefaultCellEditor(final JComboBox comboBox) {
		editorComponent = comboBox;
	}

	public DefaultCellEditor() {
	}

	/**
	 * Returns a reference to the editor component.
	 * 
	 * @return the editor <code>Component</code>
	 */
	public Component getComponent() {
		return editorComponent;
	}

	/**
	 * Specifies the number of clicks needed to start editing.
	 * 
	 * @param count
	 *            an int specifying the number of clicks needed to start editing
	 * @see #getClickCountToStart
	 */
	public void setClickCountToStart(int count) {
		clickCountToStart = count;
	}

	/**
	 * Returns the number of clicks needed to start editing.
	 * 
	 * @return the number of clicks needed to start editing
	 */
	public int getClickCountToStart() {
		return clickCountToStart;
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#getCellEditorValue
	 */
	public Object getCellEditorValue() {
		return delegate.getCellEditorValue();
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#isCellEditable(EventObject)
	 */
	public boolean isCellEditable(EventObject anEvent) {
		return delegate.isCellEditable(anEvent);
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#shouldSelectCell(EventObject)
	 */
	public boolean shouldSelectCell(EventObject anEvent) {
		return delegate.shouldSelectCell(anEvent);
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#stopCellEditing
	 */
	public boolean stopCellEditing() {
		return delegate.stopCellEditing();
	}

	/**
	 * Forwards the message from the <code>CellEditor</code> to the
	 * <code>delegate</code>.
	 * 
	 * @see EditorDelegate#cancelCellEditing
	 */
	public void cancelCellEditing() {
		delegate.cancelCellEditing();
	}

	//
	// Implementing the TreeCellEditor Interface
	//

	/** Implements the <code>TreeCellEditor</code> interface. */
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row) {
		String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, false);

		delegate.setValue(stringValue);
		return editorComponent;
	}

	//
	// Implementing the CellEditor Interface
	//
	/** Implements the <code>TableCellEditor</code> interface. */
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (this.editorComponent instanceof JComboBox && this.dynamic) {
			try {
				Object object = ((TableModel) table.getModel()).getRow(row);
				Map map = (Map) ReflectionInvoker.getProperty(object, "dynamicData");
				Vector data = (Vector) map.get(rendererData);
				JComboBox comboBox = (JComboBox) editorComponent;
				comboBox.removeAllItems();
				for (int i = 0; data != null && i < data.size(); i++) {
					comboBox.addItem(data.get(i));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		delegate.setValue(value);
		return editorComponent;
	}

	/**
	 * The protected <code>EditorDelegate</code> class.
	 */
	public class EditorDelegate implements ActionListener, ItemListener, Serializable {

		/** The value of this cell. */
		protected Object value;

		/**
		 * Returns the value of this cell.
		 * 
		 * @return the value of this cell
		 */
		public Object getCellEditorValue() {
			return value;
		}

		/**
		 * Sets the value of this cell.
		 * 
		 * @param value
		 *            the new value of this cell
		 */
		public void setValue(Object value) {
			this.value = value;
		}

		/**
		 * Returns true if <code>anEvent</code> is <b>not</b> a
		 * <code>MouseEvent</code>. Otherwise, it returns true if the necessary
		 * number of clicks have occurred, and returns false otherwise.
		 * 
		 * @param anEvent
		 *            the event
		 * @return true if cell is ready for editing, false otherwise
		 * @see #setClickCountToStart
		 * @see #shouldSelectCell
		 */
		public boolean isCellEditable(EventObject anEvent) {
			if (anEvent instanceof MouseEvent) {
				return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
			}
			return true;
		}

		/**
		 * Returns true to indicate that the editing cell may be selected.
		 * 
		 * @param anEvent
		 *            the event
		 * @return true
		 * @see #isCellEditable
		 */
		public boolean shouldSelectCell(EventObject anEvent) {
			return true;
		}

		/**
		 * Returns true to indicate that editing has begun.
		 * 
		 * @param anEvent
		 *            the event
		 */
		public boolean startCellEditing(EventObject anEvent) {
			return true;
		}

		/**
		 * Stops editing and returns true to indicate that editing has stopped.
		 * This method calls <code>fireEditingStopped</code>.
		 * 
		 * @return true
		 */
		public boolean stopCellEditing() {
			fireEditingStopped();
			return true;
		}

		/**
		 * Cancels editing. This method calls <code>fireEditingCanceled</code>.
		 */
		public void cancelCellEditing() {
			fireEditingCanceled();
		}

		/**
		 * When an action is performed, editing is ended.
		 * 
		 * @param e
		 *            the action event
		 * @see #stopCellEditing
		 */
		public void actionPerformed(ActionEvent e) {
			DefaultCellEditor.this.stopCellEditing();
		}

		/**
		 * When an item's state changes, editing is ended.
		 * 
		 * @param e
		 *            the action event
		 * @see #stopCellEditing
		 */
		public void itemStateChanged(ItemEvent e) {
			DefaultCellEditor.this.stopCellEditing();
		}
	}

	public EditorDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(EditorDelegate delegate) {
		this.delegate = delegate;
	}

} // End of class JCellEditor

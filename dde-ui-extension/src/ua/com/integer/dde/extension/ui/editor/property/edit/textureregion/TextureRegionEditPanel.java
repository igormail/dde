package ua.com.integer.dde.extension.ui.editor.property.edit.textureregion;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ua.com.integer.dde.extension.ui.UiConfig;
import ua.com.integer.dde.extension.ui.editor.property.edit.PropertyChangeListener;
import ua.com.integer.dde.extension.ui.editor.property.edit.PropertyEditComponent;
import ua.com.integer.dde.startpanel.DDEStartPanel;
import ua.com.integer.dde.startpanel.FrameTools;
import ua.com.integer.dde.startpanel.Settings;
import ua.com.integer.dde.startpanel.image.ImageSelectionListener;

public class TextureRegionEditPanel extends JPanel implements PropertyEditComponent {
	private static final long serialVersionUID = -7630890627138520749L;
	private JLabel imageLabel;
	private JLabel propertyName;
	private JButton chooseImageButton;

	private UiConfig config;
	private String uiPropertyName;
	
	private PropertyChangeListener listener;
	private JButton clearImageButton;
	private Component horizontalStrut;
	
	/**
	 * Create the panel.
	 */
	public TextureRegionEditPanel() {
		setBackground(Color.GRAY);
		setMinimumSize(new Dimension(300, 20));
		setMaximumSize(new Dimension(300, 20));
		setPreferredSize(new Dimension(300, 20));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		propertyName = new JLabel("Property name:");
		propertyName.setMaximumSize(new Dimension(100, 20));
		propertyName.setPreferredSize(new Dimension(100, 20));
		propertyName.setMinimumSize(new Dimension(100, 20));
		add(propertyName);
		
		imageLabel = new JLabel("");
		imageLabel.setForeground(Color.WHITE);
		imageLabel.setBackground(Color.WHITE);
		imageLabel.setPreferredSize(new Dimension(20, 20));
		imageLabel.setMinimumSize(new Dimension(20, 20));
		imageLabel.setMaximumSize(new Dimension(20, 20));
		add(imageLabel);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		add(horizontalGlue);
		
		chooseImageButton = new JButton("Choose...");
		chooseImageButton.addActionListener(new ChooseImageListener());
		
		clearImageButton = new JButton("Clear");
		clearImageButton.addActionListener(new ClearRegionListener());
		clearImageButton.setBackground(Color.LIGHT_GRAY);
		add(clearImageButton);
		
		horizontalStrut = Box.createHorizontalStrut(20);
		add(horizontalStrut);
		add(chooseImageButton);
		chooseImageButton.setBackground(Color.LIGHT_GRAY);

		if (DDEStartPanel.isInitialized()) {
			Settings.getInstance().setSettingsClass(DDEStartPanel.getInstance().getKernel().getClass());
		}
	}

	class ChooseImageListener extends ImageSelectionListener {
		@Override
		public void imageSelected() {
			if (config != null && uiPropertyName != null) {
				String result = getPack() + ";" + getRegion();
				config.set(uiPropertyName, result);
				
				if (listener != null) listener.propertyChanged();
			}
		}
	}
	
	class ClearRegionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (config != null && uiPropertyName != null) {
				config.properties.remove(uiPropertyName);
				
				if (listener != null) listener.propertyChanged();
			}
		}
	}
	
	@Override
	public void setConfig(UiConfig config) {
		this.config = config;
	}

	@Override
	public void setUiPropertyName(String propertyName) {
		this.uiPropertyName = propertyName;
	}

	@Override
	public void setPropertyName(String propertyName) {
		this.propertyName.setText(propertyName);
	}

	@Override
	public void setPropertyChangedListener(PropertyChangeListener listener) {
		this.listener = listener;
	}

	@Override
	public String getDefaultValue() {
		return "";
	}

	public static void main(String[] args) {
		FrameTools.testingFrame(new TextureRegionEditPanel());
	}
}

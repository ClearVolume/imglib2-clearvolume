/**
 *
 */
package de.mpicbg.jug.plugins;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import de.mpicbg.jug.clearvolume.gui.GenericClearVolumeGui;

/**
 * @author jug
 */
@Plugin( menu = { @Menu( label = "Plugins" ), @Menu( label = "ClearVolume" ) }, description = "Opens Stack in ClearVolume.", headless = false, type = Command.class )
public class ClearVolumePlugin< T extends RealType< T > & NativeType< T >> implements Command {

	@Parameter( label = "3D ImgPlus to be shown." )
	private Dataset dataset;
	private ImgPlus< T > imgPlus;

	@Parameter( label = "Window width", min = "800", required = true, stepSize = "1", columns = 5, description = "Width of the frame to be opened." )
	private int windowWidth;
	@Parameter( label = "Window height", min = "600", stepSize = "1", columns = 5, description = "Height of the frame to be opened." )
	private int windowHeight;

	@Parameter( label = "Texture width", min = "128", max = "1024", stepSize = "1", columns = 5, description = "Width of the texture to be rendered." )
	private int textureWidth;
	@Parameter( label = "Texture height", min = "128", max = "1024", stepSize = "1", columns = 5, description = "Height of the texture to be rendered." )
	private int textureHeight;


	private JFrame frame = null;
	private GenericClearVolumeGui< T > panelGui = null;

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		imgPlus = ( ImgPlus< T > ) dataset.getImgPlus();

		final boolean isShowable = checkIfShowable( imgPlus, true );

		if ( isShowable ) {
			frame = new JFrame( "ClearVolume" );
			frame.setLayout( new BorderLayout() );
			final Dimension screenDims = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
			frame.setBounds( ( screenDims.width - windowWidth ) / 2, ( screenDims.height - windowHeight ) / 2, windowWidth, windowHeight );

			panelGui = new GenericClearVolumeGui< T >( imgPlus, textureWidth, textureHeight );
			frame.add( panelGui );
			frame.revalidate();
			SwingUtilities.invokeLater( new Runnable() {

				@Override
				public void run() {
					frame.setVisible( true );
				}

			} );
		}
	}

	/**
	 * Checks if a given image has an compatible format to be shown.
	 *
	 * @param imgPlus2
	 * @return true, if image is of supported type and structure.
	 */
	private boolean checkIfShowable( final ImgPlus< T > imgPlus2, final boolean showErrorDialogs ) {
		boolean ret = true;
		String message = "";
		if ( imgPlus.numDimensions() < 3 || imgPlus.numDimensions() > 4 ) {
			message = "Only images with 3 (X,Y,Z) or 4 (X,Y,Z,C) dimensions can be shown, yours has " + imgPlus.numDimensions();
			ret = false;
		}

		if ( !message.equals( "" ) ) {
			JOptionPane.showMessageDialog( frame, message, "Image Format Error", JOptionPane.ERROR_MESSAGE );
		}

		return ret;
	}
}

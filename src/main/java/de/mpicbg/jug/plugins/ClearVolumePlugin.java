/**
 *
 */
package de.mpicbg.jug.plugins;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
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

		frame = new JFrame( "ClearVolume" );
		frame.setLayout( new BorderLayout() );
		final Dimension screenDims = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds( ( screenDims.width - windowWidth ) / 2, ( screenDims.height - windowHeight ) / 2, windowWidth, windowHeight );

		imgPlus = ( ImgPlus< T > ) dataset.getImgPlus();
		panelGui = new GenericClearVolumeGui< T >( imgPlus, textureWidth, textureHeight );
		frame.add( panelGui );
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				frame.setVisible( true );
			}

		} );
	}
}

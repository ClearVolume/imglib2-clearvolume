/**
 *
 */
package de.mpicbg.jug.clearvolume.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.apple.eawt.Application;
import com.jogamp.newt.awt.NewtCanvasAWT;

import clearvolume.renderer.panels.ControlJPanel;
import de.mpicbg.jug.clearvolume.gui.rangeslider.ClipRangeSlider;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.ColorTable;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;

/**
 * @author jug
 */
public class GenericClearVolumeGui< T extends RealType< T > & NativeType< T > >
		extends
		JPanel
		implements
		ActionListener,
		ActiveLayerListener,
		ChangeListener {

	public class LoopThread extends Thread {

		boolean doit = true;

		@Override
		public void run() {
			while ( doit ) {
				try {
					if ( fps == 0 )
						Thread.sleep( 5000 );
					else
						Thread.sleep( 1000 / fps );
					timeIndexToShow++;
					if ( timeIndexToShow > sliderTime.getMaximum() ) timeIndexToShow = 0;
					sliderTime.setValue( timeIndexToShow );
				} catch ( final InterruptedException e ) {
					e.printStackTrace();
				}
			}
		}

		public void endLooping() {
			doit = false;
		}

	}

	private Container ctnrClearVolume;
	private NewtCanvasAWT newtClearVolumeCanvas;
	private JPanel panelControls;

	private JButton buttonCredits;

	private JButton buttonResetView;
	private JButton buttonUpdateView;

	private JTextField txtVoxelSizeX;
	private JTextField txtVoxelSizeY;
	private JTextField txtVoxelSizeZ;

	private int timeIndexToShow = 0;
	private JSlider sliderTime;
	private JLabel lblTime;
	private JButton buttonPlayTime;
	private boolean bDoRenormalize;
	private JCheckBox cbRenormalizeFrames;

	private JButton buttonToggleBox;
	private JButton buttonToggleRecording;
	private List< ChannelWidget > channelWidgets;
	ControlJPanel panelClearVolumeControl;

	private int maxTextureResolution;
	private boolean useCuda;

	private ImgPlus< T > imgPlus;
	private List< RandomAccessibleInterval< T > > images;
	private ClearVolumeManager< T > cvManager;
	private LoopThread threadLoopTime;
	private JLabel lblFps;
	private JTextField txtFps;
	private int fps = 5;
	private ClipRangeSlider[] clipBoxSliders;

	/**
	 * The LUTs as they are received from the DatasetView.
	 * They are converted into ClearVolume TransferFunctions and set before
	 * rendering.
	 */
	private List< ColorTable > luts;

	public GenericClearVolumeGui( final ImgPlus< T > imgPlus ) {
		this( imgPlus, 768, true );
	}

	public GenericClearVolumeGui(
			final ImgPlus< T > imgPlus,
			final int textureResolution,
			final boolean useCuda ) {
		this( imgPlus, null, textureResolution, useCuda );
	}

	/**
	 * @param imgPlus2
	 * @param luts
	 * @param textureResolution
	 * @param useCuda
	 */
	public GenericClearVolumeGui(
			final ImgPlus< T > imgPlus,
			final List< ColorTable > luts,
			final int textureResolution,
			final boolean useCuda ) {
		super( true );

		this.imgPlus = imgPlus;
		this.luts = luts;
		images = new ArrayList< RandomAccessibleInterval< T >>();
		setTextureSizeAndCudaFlag( textureResolution, useCuda );

		if ( imgPlus != null ) {
			setImagesFromImgPlus( imgPlus );
			launchClearVolumeManager();
		}
	}

	private void setImagesFromImgPlus( final ImgPlus< T > imgPlus ) {
		if ( imgPlus == null )
			return;

		final int dX = imgPlus.dimensionIndex( Axes.X );
		final int dY = imgPlus.dimensionIndex( Axes.Y );
		final int dZ = imgPlus.dimensionIndex( Axes.Z );
		final int dC = imgPlus.dimensionIndex( Axes.CHANNEL );
		final int dT = imgPlus.dimensionIndex( Axes.TIME );

		if ( imgPlus.numDimensions() == 2 ) {
			if ( dX >= 0 && dY >= 0 ) {
				images.add( imgPlus );
			} else {
				throw new IllegalArgumentException( "2 dimensional input image must have X and Y axes." );
			}
		} else if ( imgPlus.numDimensions() == 3 ) {
			if ( dX >= 0 && dY >= 0 && dZ >= 0 ) {
				images.add( imgPlus );
			} else if ( dX >= 0 && dY >= 0 && dC >= 0 ) {
				addImagePerChannel( imgPlus, dC );
			} else
			if ( dX >= 0 && dY >= 0 && dT >= 0 ) {
				extractChannelsAtT( timeIndexToShow, dC, dT );
			} else {
				throw new IllegalArgumentException( "3 dimensional input image must have X and Y axes plus either Z, CHANNEL, or TIME." );
			}
		} else if ( imgPlus.numDimensions() == 4 ) {
			if ( dX >= 0 && dY >= 0 && dZ >= 0 && dC >= 0 && dT < 0 ) {
				addImagePerChannel( imgPlus, dC );
			} else
			if ( dX >= 0 && dY >= 0 && dZ >= 0 && dC < 0 && dT >= 0 ) {
				extractChannelsAtT( timeIndexToShow, dC, dT );
			} else {
				throw new IllegalArgumentException( "4 dimensional input image must have X, Y and Z axes plus either CHANNEL, or TIME." );
			}
		} else if ( imgPlus.numDimensions() == 5 ) {
			if ( dX >= 0 && dY >= 0 && dZ >= 0 && dC >= 0 && dT >= 0 ) {
				extractChannelsAtT( timeIndexToShow, dC, dT );
			} else {
				throw new IllegalArgumentException( "Five dimensional input image must contain X,Y,Z,CHANNEL, and TIME axes!" );
			}
		} else {
			throw new IllegalArgumentException( "Only 2 to 5 dimensional images are currently supported." );
		}
	}

	/**
	 * @param imgPlus2
	 */
	private void addImagePerChannel( final RandomAccessibleInterval< T > imgPlus, final int dC ) {
		final List< RandomAccessibleInterval< T > > newimages =
				new ArrayList< RandomAccessibleInterval< T > >();

		for ( int channel = 0; channel < imgPlus.dimension( dC ); channel++ ) {
			final RandomAccessibleInterval< T > rai =
					Views.hyperSlice( imgPlus, dC, channel );
			newimages.add( rai );
		}
		images = newimages;
	}

	/**
	 * @param t
	 */
	public void extractChannelsAtT( final int t ) {
		final int dC = imgPlus.dimensionIndex( Axes.CHANNEL );
		final int dT = imgPlus.dimensionIndex( Axes.TIME );
		extractChannelsAtT( t, dC, dT );
	}

	/**
	 *
	 * @param t
	 * @param dC
	 * @param dT
	 */
	public void extractChannelsAtT(
			final int t,
			final int dC,
			final int dT ) {
		final RandomAccessibleInterval< T > timePointToShow = Views.hyperSlice(
				imgPlus,
				dT,
				t );
		if ( dC == -1 ) {
			final ArrayList< RandomAccessibleInterval< T > > newimage =
					new ArrayList< RandomAccessibleInterval< T > >();
			newimage.add( timePointToShow );
			images = newimage;
		} else {
			addImagePerChannel( timePointToShow, dC );
		}
	}

	public void launchClearVolumeManager() {
		// if cvManager is set from previous session - free everything!
		if ( cvManager != null ) {
			cvManager.close();
			this.closeOldSession();
		}

		// instantiate a NEW ClearVolumeManager
		try {
			final GenericClearVolumeGui< T > self = this;
			final Runnable todo = new Runnable() {

				@Override
				public void run() {
					cvManager =
							new ClearVolumeManager< T >( images, luts, maxTextureResolution, maxTextureResolution, useCuda );
					cvManager.addActiveLayerChangedListener( self );
				}
			};

			if ( javax.swing.SwingUtilities.isEventDispatchThread() ) {
				todo.run();
			} else {
				SwingUtilities.invokeAndWait( todo );
			}
		} catch ( final Exception e ) {
			System.err.println( "Launching CV session was interrupted in GenericClearVolumeGui!" );
			e.printStackTrace();
		}

		final int dX = imgPlus.dimensionIndex( Axes.X );
		final int dY = imgPlus.dimensionIndex( Axes.Y );
		final int dZ = imgPlus.dimensionIndex( Axes.Z );
		if ( dX != -1 && dY != -1 && dZ != -1 ) {
			cvManager.setVoxelSize(
					imgPlus.averageScale( imgPlus.dimensionIndex( Axes.X ) ),
					imgPlus.averageScale( imgPlus.dimensionIndex( Axes.Y ) ),
					imgPlus.averageScale( imgPlus.dimensionIndex( Axes.Z ) ) );
		} else if ( imgPlus.numDimensions() >= 3 ) {
			cvManager.setVoxelSize(
					imgPlus.averageScale( 0 ),
					imgPlus.averageScale( 1 ),
					imgPlus.averageScale( 2 ) );
		}
		cvManager.run();

		// Create necessary channel widgets!
		this.channelWidgets = new ArrayList< ChannelWidget >();
		for ( int i = 0; i < images.size(); i++ ) {
			channelWidgets.add( new ChannelWidget( cvManager, i ) );
		}

		buildGui();
	}

	public void relaunchClearVolumeManager( final ClearVolumeManager< T > oldManager ) {
		final double[] oldMinI = oldManager.getMinIntensities();
		final double[] oldMaxI = oldManager.getMaxIntensities();
		final List< RandomAccessibleInterval< T > > oldImages = oldManager.getChannelImages();
		final double oldVoxelSizeX = oldManager.getVoxelSizeX();
		final double oldVoxelSizeY = oldManager.getVoxelSizeY();
		final double oldVoxelSizeZ = oldManager.getVoxelSizeZ();

		oldManager.close();
		this.closeOldSession();

		// instantiate a NEW ClearVolumeManager using the old images and params
		try {
			final GenericClearVolumeGui< T > self = this;
			final Runnable todo = new Runnable() {

				@Override
				public void run() {
					cvManager =
							new ClearVolumeManager< T >( oldImages, luts, maxTextureResolution, maxTextureResolution, useCuda );
					cvManager.addActiveLayerChangedListener( self );
				}
			};

			if ( javax.swing.SwingUtilities.isEventDispatchThread() ) {
				todo.run();
			} else {
				SwingUtilities.invokeAndWait( todo );
			}
		} catch ( final Exception e ) {
			System.err
					.println( "Relaunching CV session was interrupted in GenericClearVolumeGui!" );
		}

		cvManager.setVoxelSize(
				oldVoxelSizeX,
				oldVoxelSizeY,
				oldVoxelSizeZ );
		for ( int i = 0; i < oldImages.size(); i++ ) {
			cvManager.setIntensityValues( i, oldMinI[ i ], oldMaxI[ i ] );
		}

		cvManager.run();
		buildGui();
	}

	private void setTextureSizeAndCudaFlag(
			final int textureRes,
			final boolean useCuda ) {
		this.maxTextureResolution = textureRes;
		this.useCuda = useCuda;

		if ( cvManager != null ) {
			cvManager.setTextureSize( textureRes, textureRes );
			cvManager.setCuda( true );
		}
	}

	public ClearVolumeManager< T > getClearVolumeManager() {
		return cvManager;
	}

	public void pushParamsToGui() {
		txtVoxelSizeX.setText( "" + cvManager.getVoxelSizeX() );
		txtVoxelSizeY.setText( "" + cvManager.getVoxelSizeY() );
		txtVoxelSizeZ.setText( "" + cvManager.getVoxelSizeZ() );

		final float[] clipbox = cvManager.getClipBox();

		// System.out.println("pushing:");
		// for (int j = 0; j < clipBoxSliders.length; j++)
		// {
		// clipBoxSliders[j].setValueLower(clipbox[2 * j]);
		// clipBoxSliders[j].setValueUpper(clipbox[2 * j + 1]);
		// }

	}

	/**
	 * Read all validly entered text field values and activate them.
	 */
	private void activateGuiValues() {
		final int i;
		double d;

		try {
			d = Double.parseDouble( txtVoxelSizeX.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getVoxelSizeX();
		}
		final double voxelSizeX = d;

		try {
			d = Double.parseDouble( txtVoxelSizeY.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getVoxelSizeY();
		}
		final double voxelSizeY = d;

		try {
			d = Double.parseDouble( txtVoxelSizeZ.getText() );
		} catch ( final NumberFormatException e ) {
			d = cvManager.getVoxelSizeZ();
		}
		final double voxelSizeZ = d;

		cvManager.setVoxelSize( voxelSizeX, voxelSizeY, voxelSizeZ );

	}

	private void buildGui() {
		// this.setIgnoreRepaint( true );
		this.setVisible( false );
		this.removeAll();

		this.setLayout( new BorderLayout() );

		ctnrClearVolume = new Container();
		ctnrClearVolume.setLayout( new BorderLayout() );

		if ( cvManager != null ) {
			newtClearVolumeCanvas = cvManager
					.getClearVolumeRendererInterface()
					.getNewtCanvasAWT();
			ctnrClearVolume.add( newtClearVolumeCanvas, BorderLayout.CENTER );

			panelClearVolumeControl =
					new ControlJPanel( cvManager.getActiveChannelIndex(), cvManager
							.getClearVolumeRendererInterface() );
			panelClearVolumeControl
					.setClearVolumeRendererInterface( cvManager.getClearVolumeRendererInterface() );
		} else {
			System.err.println(
					"ClearVolumeTableCellView: Did you intend this? You called buildGui while cvManager==null!" );
		}

		// Main controls panel
		// -------------------
		panelControls = new JPanel();
		panelControls.setLayout( new BoxLayout( panelControls, BoxLayout.Y_AXIS ) );
		panelControls.add( Box.createVerticalGlue() );

		// Credits baby!!!
		buttonCredits = new JButton( "Help + how to cite us!" );
		buttonCredits.setForeground( Color.darkGray );
		buttonCredits.addActionListener( this );

		final JPanel panelCreditsHelper = new JPanel( new GridLayout( 1, 1 ) );
		panelCreditsHelper.setBorder( BorderFactory.createEmptyBorder(
				5,
				5,
				2,
				2 ) );

		panelCreditsHelper.add( buttonCredits );

		JPanel shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( panelCreditsHelper, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder(
				0,
				5,
				2,
				2 ) );
		panelControls.add( shrinkingHelper );

		// Parameters that require a view update
		// -------------------------------------
		JPanel panelControlsHelper = new JPanel( new GridLayout( 3, 2 ) );
		panelControlsHelper.setBorder( BorderFactory.createEmptyBorder(
				0,
				5,
				2,
				2 ) );

		final JLabel lblVoxelSizeX = new JLabel( "VoxelDimension.X" );
		txtVoxelSizeX = new JTextField( 8 );
		txtVoxelSizeX.setActionCommand( "UpdateView" );
		txtVoxelSizeX.addActionListener( this );
		final JLabel lblVoxelSizeY = new JLabel( "VoxelDimension.Y" );
		txtVoxelSizeY = new JTextField( 8 );
		txtVoxelSizeY.setActionCommand( "UpdateView" );
		txtVoxelSizeY.addActionListener( this );
		final JLabel lblVoxelSizeZ = new JLabel( "VoxelDimension.Z" );
		txtVoxelSizeZ = new JTextField( 8 );
		txtVoxelSizeZ.setActionCommand( "UpdateView" );
		txtVoxelSizeZ.addActionListener( this );

		panelControlsHelper.add( lblVoxelSizeX );
		panelControlsHelper.add( txtVoxelSizeX );
		panelControlsHelper.add( lblVoxelSizeY );
		panelControlsHelper.add( txtVoxelSizeY );
		panelControlsHelper.add( lblVoxelSizeZ );
		panelControlsHelper.add( txtVoxelSizeZ );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( panelControlsHelper, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder(
				0,
				5,
				2,
				2 ) );
		panelControls.add( shrinkingHelper );

		buttonUpdateView = new JButton( "Set" );
		buttonUpdateView.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonUpdateView, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder(
				0,
				5,
				2,
				2 ) );
		panelControls.add( shrinkingHelper );

		buttonResetView = new JButton( "Reset" );
		buttonResetView.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonResetView, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder(
				0,
				5,
				11,
				2 ) );
		panelControls.add( shrinkingHelper );

		// Crop Box
		// --------------
		final JPanel panelSliderHelper = new JPanel( new GridLayout( 3, 2 ) );
		panelSliderHelper.setBorder( BorderFactory.createTitledBorder( "Crop box" ) );

		// create the 3 sliders that will affect the clipping box
		clipBoxSliders = new ClipRangeSlider[] { new ClipRangeSlider(),
												 new ClipRangeSlider(),
												 new ClipRangeSlider() };

		for ( final ClipRangeSlider slide : clipBoxSliders ) {
			shrinkingHelper = new JPanel( new BorderLayout() );
			shrinkingHelper.add( slide, BorderLayout.SOUTH );
			shrinkingHelper.setBorder( BorderFactory.createEmptyBorder(
					0,
					5,
					2,
					2 ) );
			panelSliderHelper.add( shrinkingHelper );

			System.out.println( slide.getValueUpper() );
			slide.addChangeListener( this );

			slide.addChangeListener( new ChangeListener() {

				@Override
				public void stateChanged( final ChangeEvent e ) {
					final float[] clipbox = new float[ 6 ];

					for ( int j = 0; j < clipBoxSliders.length; j++ ) {
						clipbox[ 2 * j ] = ( clipBoxSliders[ j ].getValueLower() );
						clipbox[ 2 * j + 1 ] = ( clipBoxSliders[ j ].getValueUpper() );
					}

					cvManager.setClipBox( clipbox );

				}
			} );
		}
		panelControls.add( panelSliderHelper );

		// Toggle-buttons
		// --------------
		buttonToggleBox = new JButton( "Show/Unshow Box" );
		buttonToggleBox.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonToggleBox, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder(
				11,
				5,
				2,
				2 ) );
		panelControls.add( shrinkingHelper );

		buttonToggleRecording = new JButton( "Start/Stop Recording" );
		buttonToggleRecording.addActionListener( this );

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( buttonToggleRecording, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder(
				0,
				5,
				22,
				2 ) );
		panelControls.add( shrinkingHelper );

		// Channel Widgets
		// ===============
		panelControlsHelper = new JPanel( new GridLayout( channelWidgets.size(), 1 ) );
		panelControlsHelper.setBorder( BorderFactory.createTitledBorder( "Channels" ) );

		for ( int i = 0; i < channelWidgets.size(); i++ ) {
			panelControlsHelper.add( channelWidgets.get( i ) );
		}
		channelWidgets.get( 0 ).addSelectionVisuals();

		shrinkingHelper = new JPanel( new BorderLayout() );
		shrinkingHelper.add( panelControlsHelper, BorderLayout.SOUTH );
		shrinkingHelper.setBorder( BorderFactory.createEmptyBorder(
				0,
				5,
				2,
				2 ) );
		panelControls.add( shrinkingHelper );

		// Time related
		// ============
		if ( imgPlus.dimensionIndex( Axes.TIME ) != -1 ) {
			panelControlsHelper = new JPanel( new MigLayout() );
			panelControlsHelper.setBorder( BorderFactory.createTitledBorder( "Time" ) );

			lblTime = new JLabel( String.format( "t=%02d", ( timeIndexToShow + 1 ) ) );
			lblFps = new JLabel( "fps:" );

			txtFps = new JTextField( 2 );
			txtFps.setText( "" + fps );
			txtFps.addActionListener( this );

			sliderTime =
					new JSlider( 0, ( int ) imgPlus.max( imgPlus.dimensionIndex( Axes.TIME ) ), 0 );
			sliderTime.addChangeListener( this );
			buttonPlayTime = new JButton();
			buttonPlayTime.addActionListener( this );
			setIcon( buttonPlayTime, "play.gif", ">", Color.BLUE );
			cbRenormalizeFrames = new JCheckBox( "normalize each time-point" );
			cbRenormalizeFrames.addActionListener( this );

			panelControlsHelper.add( lblTime );
			panelControlsHelper.add( buttonPlayTime );
			panelControlsHelper.add( sliderTime, "span, wrap" );
			panelControlsHelper.add( lblFps );
			panelControlsHelper.add( txtFps );
			panelControlsHelper.add( cbRenormalizeFrames );

			shrinkingHelper = new JPanel( new BorderLayout() );
			shrinkingHelper.add( panelControlsHelper, BorderLayout.SOUTH );
			shrinkingHelper.setBorder( BorderFactory.createEmptyBorder(
					0,
					5,
					2,
					2 ) );
			panelControls.add( shrinkingHelper );
		}

		// Display hijacked control container if possible
		// ----------------------------------------------
		if ( panelClearVolumeControl != null ) {
			this.add( panelClearVolumeControl, BorderLayout.SOUTH );
		}

		this.add( ctnrClearVolume, BorderLayout.CENTER );

		final JPanel helperPanel = new JPanel( new BorderLayout() );
		helperPanel.add( panelControls, BorderLayout.NORTH );

		final JScrollPane scrollPane = new JScrollPane( helperPanel );
		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
		this.add( scrollPane, BorderLayout.EAST );

		// Update the values in the gui fields
		pushParamsToGui();

		// this.setIgnoreRepaint( false );
		this.setVisible( true );
	}

	/**
	 * Cleans up all ClearVolume resources and empties this panel.
	 */
	public void closeOldSession() {
		try {
			final GenericClearVolumeGui< T > self = this;
			final Runnable todo = new Runnable() {

				@Override
				public void run() {
					if ( newtClearVolumeCanvas != null )
						ctnrClearVolume.remove( newtClearVolumeCanvas );
					if ( cvManager != null )
						cvManager.close();
					self.removeAll();
				}
			};

			if ( javax.swing.SwingUtilities.isEventDispatchThread() ) {
				todo.run();
			} else {
				SwingUtilities.invokeAndWait( todo );
			}
		} catch ( final Exception e ) {
			System.err.println(
					"Closing of an old CV session was interrupted in GenericClearVolumeGui!" );
		}
	}

	/**
	 * @see de.mpicbg.jug.clearvolume.gui.ActiveLayerListener#activeLayerChanged(int)
	 */
	@Override
	public void activeLayerChanged( final int layerId ) {
		int i = 0;
		for ( final ChannelWidget cw : channelWidgets ) {
			if ( i != layerId ) {
				cw.removeSelectionVisuals();
			}
			i++;
		}

		this.remove( panelClearVolumeControl );
		panelClearVolumeControl = new ControlJPanel( cvManager.getActiveChannelIndex(), cvManager
				.getClearVolumeRendererInterface() );
		this.add( panelClearVolumeControl, BorderLayout.SOUTH );

		final GenericClearVolumeGui< T > self = this;
		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				self.revalidate();
			}
		} );
	}

	/**
	 * Call to retrieve the current app image. This will help you to circumvent
	 * the jogl icon stealing bullshit!
	 *
	 * @return
	 */
	public static Image getCurrentAppIcon() {
		final String os = System.getProperty( "os.name" ).toLowerCase();
		Image icon = null;
		if ( os.indexOf( "mac" ) >= 0 ) {
			icon = Application.getApplication().getDockIconImage();
		} else if ( os.indexOf( "win" ) >= 0 ) {
			// not yet clear
			icon = null;
		} else {
			// not yet clear
			icon = null;
		}
		return icon;
	}

	/**
	 * @param finalicon
	 */
	public static void setCurrentAppIcon( final Image finalicon ) {
		final String os = System.getProperty( "os.name" ).toLowerCase();

		if ( finalicon == null )
			return;

		SwingUtilities.invokeLater( new Runnable() {

			@Override
			public void run() {
				if ( os.indexOf( "mac" ) >= 0 ) {
					Application.getApplication().setDockIconImage( finalicon );
				} else if ( os.indexOf( "win" ) >= 0 ) {
					// not yet clear
				} else {
					// not yet clear
				}
			}
		} );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed( final ActionEvent e ) {

		if ( e.getSource().equals( buttonUpdateView ) || e
				.getActionCommand()
				.equals( "UpdateView" ) ) {
			activateGuiValues();
			cvManager.updateView();
		} else if ( e.getSource().equals( buttonResetView ) ) {
			cvManager.setVoxelSize(
					imgPlus.averageScale( imgPlus.dimensionIndex( Axes.X ) ),
					imgPlus.averageScale( imgPlus.dimensionIndex( Axes.Y ) ),
					imgPlus.averageScale( imgPlus.dimensionIndex( Axes.Z ) ) );
			pushParamsToGui();
			cvManager.resetView();
		} else if ( e.getSource().equals( buttonToggleBox ) ) {
			cvManager.toggleBox();
		} else if ( e.getSource().equals( buttonToggleRecording ) ) {
			cvManager.toggleRecording();
		} else if ( e.getSource().equals( cbRenormalizeFrames ) ) {
			bDoRenormalize = cbRenormalizeFrames.isSelected();
			extractChannelsAtT( timeIndexToShow );
			showExtractedChannels();
		} else if ( e.getSource().equals( txtFps ) ) {
			try {
				fps = Integer.parseInt( txtFps.getText() );
			} catch ( final NumberFormatException nfe ) {
				// fps = fps;
			}
		} else if ( e.getSource().equals( buttonPlayTime ) ) {
			if ( threadLoopTime == null ) {
				setIcon( buttonPlayTime, "pause.gif", "X", Color.BLUE );
				threadLoopTime = new LoopThread();
				threadLoopTime.start();
			} else {
				threadLoopTime.endLooping();
				threadLoopTime = null;
				setIcon( buttonPlayTime, "play.gif", ">", Color.BLUE );
			}
		} else if ( e.getSource().equals( buttonCredits ) ) {
			new CreditsDialog( this );
		}

	}

	/**
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged( final ChangeEvent e ) {
		if ( e.getSource().equals( sliderTime ) ) {
			timeIndexToShow = sliderTime.getValue();
			lblTime.setText( String.format( "t=%02d", ( timeIndexToShow + 1 ) ) );

			extractChannelsAtT( timeIndexToShow );
			showExtractedChannels();
		}
	}

	/**
	 * Updates an validly initialized ClearVolume Manager so that he shows the
	 * data in local field <code>images</code>.
	 */
	private void showExtractedChannels() {
		cvManager.updateImages( images, bDoRenormalize );
	}

	/**
	 */
	private void setIcon(
			final JButton button,
			final String filename,
			final String altText,
			final Color altColor ) {
		try {
			URL iconURL = ClassLoader.getSystemResource( filename );
			if ( iconURL == null ) {
				iconURL = getClass().getClassLoader().getResource( filename );
			}
			final Image img = ImageIO.read( iconURL );
			button.setIcon( new ImageIcon( img.getScaledInstance(
					20,
					20,
					java.awt.Image.SCALE_SMOOTH ) ) );
		} catch ( final Exception e ) {
			e.printStackTrace();
			button.setText( altText );
			button.setForeground( altColor );
		}
	}

}

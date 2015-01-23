package simpleTests;
/**
 *
 */


import ij.IJ;

import java.io.File;
import java.nio.ByteBuffer;

import net.imglib2.RandomAccess;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import clearvolume.ClearVolume;
import clearvolume.ClearVolumeUnsignedShortType;
import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.transferf.TransferFunctions;


/**
 * @author jug
 */
public class testOpenClearVolumeOnToyVolume {

	public static void main( final String[] args ) {
//		goBasicStyle();
//		goImgLibByteType();
//		goImgLibUnsignedShortType();
//		goImgLibClearVolumeUnsignedShortType();
//		goImgLibDoubleType();
//		showNordenImg();
//		showMansfeldImg();
		showDrosoImg();
	}

	private static void showDrosoImg() {
		final File file = new File( "/Users/jug/Desktop/droso.tif" );

		System.out.print( "\n >> Loading file '" + file.getName() + "' ..." );
		final long tic = System.currentTimeMillis();

//		final Img< FloatType > img = IO.openFloatImgs( file.getPath() ).get( 0 );
		final Img< FloatType > img = ImagePlusAdapter.wrapFloat( IJ.openImage( file.getAbsolutePath() ) );

		final long toc = System.currentTimeMillis();
		System.out.println( String.format( " ...done in %d ms.", toc - tic ) );

		Normalize.normalize( img, new FloatType( 0f ), new FloatType( 1f ) );
		final ClearVolumeRendererInterface cv = ClearVolume.initRealImg( img, "Img -> ClearVolume", 1024, 1024, 1024, 1024, 0., 1.0 );
		cv.setVoxelSize( 1., 1., 4. );
		cv.requestDisplay();

		while ( cv.isShowing() ) {
			try {
				Thread.sleep( 500 );
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
		}
		cv.close();
	}

	private static void showMansfeldImg() {
		final File file = new File("/Users/jug/MPI/ProjectMansfeld/Movie01/Hist3_mturq2_800.tif");

		System.out.print( "\n >> Loading file '" + file.getName() + "' ..." );
		final long tic = System.currentTimeMillis();

//		final ImgFactory< UnsignedShortType > imgFactory = new ArrayImgFactory< UnsignedShortType >();
//		final Img< UnsignedShortType > img = ( Img< UnsignedShortType > ) IO.openImgs( file.getPath(), imgFactory ).get( 0 );
		final Img< UnsignedShortType > img = ImagePlusAdapter.wrapNumeric( IJ.openImage( "/Users/jug/MPI/ProjectMansfeld/Movie01/Hist3_mturq2_800.tif" ) );

		final long toc = System.currentTimeMillis();
		System.out.println( String.format( " ...done in %d ms.", toc - tic ) );

//		Normalize.normalize( img, new UnsignedShortType( 0 ), new UnsignedShortType( 65636 ) );
		final ClearVolumeRendererInterface cv = ClearVolume.showUnsignedShortArrayImgWindow( ( ArrayImg< UnsignedShortType, ShortArray > ) img, "Img -> ClearVolume", 1024, 1024, 1024, 1024 );

		while ( cv.isShowing() ) {
			try {
				Thread.sleep( 500 );
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
		}
		cv.close();
	}

	private static void showNordenImg() {
		final Img< DoubleType > img = ImagePlusAdapter.wrapReal( IJ.openImage( "/Users/jug/Desktop/norden.tif" ) );
//		Normalize.normalize( img, new DoubleType( 0. ), new DoubleType( 1. ) );
		final ClearVolumeRendererInterface cv = ClearVolume.showRealImg( img, "Img -> ClearVolume", 512, 512, 512, 512, 0., 300.0 );

		while ( cv.isShowing() ) {
			try {
				Thread.sleep( 500 );
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
		}
		cv.close();
	}

	private static void goImgLibDoubleType() {

		// Data to show

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		double max = 0.;

		final ArrayImg< DoubleType, DoubleArray > imgVolumeDataArray = ArrayImgs.doubles( new long[] { lResolutionX, lResolutionY, lResolutionZ } );
		final RandomAccess< DoubleType > raImg = imgVolumeDataArray.randomAccess();

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
					if (lCharValue < 12)
						lCharValue = 0;
					raImg.setPosition( x, 0 );
					raImg.setPosition( y, 1 );
					raImg.setPosition( z, 2 );
					raImg.get().set( lCharValue );
					max = Math.max( max, lCharValue );
				}

		// Show
		final ClearVolumeRendererInterface cv = ClearVolume.showRealArrayImg( imgVolumeDataArray, "Img -> ClearVolume", 512, 512, 512, 512, 0., max );
		while ( cv.isShowing() ) {
			try {
				Thread.sleep( 500 );
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
		}
		cv.close();
	}

	private static void goImgLibClearVolumeUnsignedShortType() {

		// Data to show

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final Img< ClearVolumeUnsignedShortType > imgVolumeDataArray =
				new ArrayImgFactory< ClearVolumeUnsignedShortType >().create( new int[] {lResolutionX, lResolutionY, lResolutionZ},
						new ClearVolumeUnsignedShortType() );
		final RandomAccess< ClearVolumeUnsignedShortType > raImg = imgVolumeDataArray.randomAccess();

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
					if (lCharValue < 12)
						lCharValue = 0;
					raImg.setPosition( x, 0 );
					raImg.setPosition( y, 1 );
					raImg.setPosition( z, 2 );
					raImg.get().set( ( short ) lCharValue );
				}

		// Show
		final ClearVolumeRendererInterface cv = ClearVolume.showClearVolumeUnsignedShortArrayImgWindow( ( ArrayImg< ClearVolumeUnsignedShortType, ByteArray > ) imgVolumeDataArray, "Img -> ClearVolume", 512, 512, 512, 512 );
		while ( cv.isShowing() ) {
			try {
				Thread.sleep( 500 );
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
		}
		cv.close();
	}

	private static void openImgLibUnsignedShortTypeDemo() {

		// Data to show

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final ArrayImg< UnsignedShortType, ShortArray > imgVolumeDataArray = ArrayImgs.unsignedShorts( lResolutionX, lResolutionY, lResolutionZ );
		final RandomAccess< UnsignedShortType > raImg = imgVolumeDataArray.randomAccess();

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
					if (lCharValue < 12)
						lCharValue = 0;
					raImg.setPosition( x, 0 );
					raImg.setPosition( y, 1 );
					raImg.setPosition( z, 2 );
					raImg.get().set( ( short ) lCharValue );
				}

		// Show
		final ClearVolumeRendererInterface cv = ClearVolume.showUnsignedShortArrayImgWindow( imgVolumeDataArray, "Img -> ClearVolume", 512, 512, 512, 512 );
		while ( cv.isShowing() ) {
			try {
				Thread.sleep( 500 );
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
		}
		cv.close();
	}

	private static void openImgLibByteTypeDemo() {

		// Data to show

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final ArrayImg< ByteType, ByteArray > imgVolumeDataArray = ArrayImgs.bytes( lResolutionX, lResolutionY, lResolutionZ );
		final RandomAccess< ByteType > raImg = imgVolumeDataArray.randomAccess();

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x + lResolutionX
							* y
							+ lResolutionX
							* lResolutionY
							* z;
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
					if (lCharValue < 12)
						lCharValue = 0;
					raImg.setPosition( x, 0 );
					raImg.setPosition( y, 1 );
					raImg.setPosition( z, 2 );
					raImg.get().set( ( byte ) lCharValue );
				}

		// Show
		final ClearVolumeRendererInterface cv = ClearVolume.initByteArrayImgWindow( imgVolumeDataArray, "Img -> ClearVolume", 512, 512, 512, 512 );
		while ( cv.isShowing() ) {
			try {
				Thread.sleep( 500 );
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
		}
		cv.close();
	}

	public static void showClearVolumeDemo() {

		// Data to show

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final byte[] lVolumeDataArray = new byte[lResolutionX * lResolutionY
		                                         * lResolutionZ];

		for (int z = 0; z < lResolutionZ; z++)
			for (int y = 0; y < lResolutionY; y++)
				for (int x = 0; x < lResolutionX; x++)
				{
					final int lIndex = x + lResolutionX
							* y
							+ lResolutionX
							* lResolutionY
							* z;
					int lCharValue = (((byte) x ^ (byte) y ^ (byte) z));
					if (lCharValue < 12)
						lCharValue = 0;
					lVolumeDataArray[lIndex] = (byte) lCharValue;
				}

		// Show
		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(	"ClearVolumeTest",
				1024,
				1024,
				1,
				512,
				512,
				1);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		lClearVolumeRenderer.setCurrentRenderLayer(0);
		lClearVolumeRenderer.setVolumeDataBuffer(	ByteBuffer.wrap(lVolumeDataArray),
				lResolutionX,
				lResolutionY,
				lResolutionZ);
		lClearVolumeRenderer.requestDisplay();

		while (lClearVolumeRenderer.isShowing())
		{
			try {
				Thread.sleep(500);
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
		}

		lClearVolumeRenderer.close();
	}
}

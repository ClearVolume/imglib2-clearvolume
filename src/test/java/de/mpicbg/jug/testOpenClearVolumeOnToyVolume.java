package de.mpicbg.jug;

/**
 *
 */

import ij.IJ;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.transferf.TransferFunctions;
import de.mpicbg.jug.clearvolume.ImgLib2ClearVolume;

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
		final Img< FloatType > img =
				ImagePlusAdapter.wrapFloat( IJ.openImage( file.getAbsolutePath() ) );

		final List< RandomAccessibleInterval< FloatType >> imgs =
				new ArrayList< RandomAccessibleInterval< FloatType >>();
		imgs.add( img );

		final long toc = System.currentTimeMillis();
		System.out.println( String.format( " ...done in %d ms.", toc - tic ) );

		Normalize.normalize( img, new FloatType( 0f ), new FloatType( 1f ) );
		final ClearVolumeRendererInterface cv =
				ImgLib2ClearVolume.initRealImgs(
						imgs,
						"Img -> ClearVolume",
						512, 512,
						512, 512,
						false,
						new double[] { 0. },
						new double[] { 1.0 } );
		cv.setVoxelSize( 1., 1., 3.5 );
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

	private static void openImgLibUnsignedShortTypeDemo() {

		// Data to show

		final int lResolutionX = 256;
		final int lResolutionY = lResolutionX;
		final int lResolutionZ = lResolutionX;

		final ArrayImg< UnsignedShortType, ShortArray > imgVolumeDataArray =
				ArrayImgs.unsignedShorts( lResolutionX, lResolutionY, lResolutionZ );
		final RandomAccess< UnsignedShortType > raImg = imgVolumeDataArray.randomAccess();

		final List< ArrayImg< UnsignedShortType, ShortArray >> imgs =
				new ArrayList< ArrayImg< UnsignedShortType, ShortArray > >();
		imgs.add( imgVolumeDataArray );

		for ( int z = 0; z < lResolutionZ; z++ )
			for ( int y = 0; y < lResolutionY; y++ )
				for ( int x = 0; x < lResolutionX; x++ )
				{
					int lCharValue = ( ( ( byte ) x ^ ( byte ) y ^ ( byte ) z ) );
					if ( lCharValue < 12 )
						lCharValue = 0;
					raImg.setPosition( x, 0 );
					raImg.setPosition( y, 1 );
					raImg.setPosition( z, 2 );
					raImg.get().set( ( short ) lCharValue );
				}

		// Show
		final ClearVolumeRendererInterface cv =
				ImgLib2ClearVolume.showUnsignedShortArrayImgs(
						imgs,
						"Img -> ClearVolume",
						512,
						512,
						512,
						512,
						false );
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

		final ArrayImg< ByteType, ByteArray > imgVolumeDataArray =
				ArrayImgs.bytes( lResolutionX, lResolutionY, lResolutionZ );
		final RandomAccess< ByteType > raImg = imgVolumeDataArray.randomAccess();

		final List< ArrayImg< ByteType, ByteArray >> imgs =
				new ArrayList< ArrayImg< ByteType, ByteArray >>();
		imgs.add( imgVolumeDataArray );

		for ( int z = 0; z < lResolutionZ; z++ )
			for ( int y = 0; y < lResolutionY; y++ )
				for ( int x = 0; x < lResolutionX; x++ )
				{
					final int lIndex = x + lResolutionX
							* y
							+ lResolutionX
							* lResolutionY
							* z;
					int lCharValue = ( ( ( byte ) x ^ ( byte ) y ^ ( byte ) z ) );
					if ( lCharValue < 12 )
						lCharValue = 0;
					raImg.setPosition( x, 0 );
					raImg.setPosition( y, 1 );
					raImg.setPosition( z, 2 );
					raImg.get().set( ( byte ) lCharValue );
				}

		// Show
		final ClearVolumeRendererInterface cv =
				ImgLib2ClearVolume.initByteArrayImgs(
						imgs,
						"Img -> ClearVolume",
						512,
						512,
						512,
						512,
						false );
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

		final byte[] lVolumeDataArray = new byte[ lResolutionX * lResolutionY
				* lResolutionZ ];

		for ( int z = 0; z < lResolutionZ; z++ )
			for ( int y = 0; y < lResolutionY; y++ )
				for ( int x = 0; x < lResolutionX; x++ )
				{
					final int lIndex = x + lResolutionX
							* y
							+ lResolutionX
							* lResolutionY
							* z;
					int lCharValue = ( ( ( byte ) x ^ ( byte ) y ^ ( byte ) z ) );
					if ( lCharValue < 12 )
						lCharValue = 0;
					lVolumeDataArray[ lIndex ] = ( byte ) lCharValue;
				}

		// Show
		final ClearVolumeRendererInterface lClearVolumeRenderer =
				ClearVolumeRendererFactory.newBestRenderer( "ClearVolumeTest",
						1024,
						1024,
						1,
						512,
						512,
						1,
						false );
		lClearVolumeRenderer.setTransferFunction( TransferFunctions.getGrayLevel() );
		lClearVolumeRenderer.setVisible( true );

		lClearVolumeRenderer.setCurrentRenderLayer( 0 );
		lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( lVolumeDataArray ),
				lResolutionX,
				lResolutionY,
				lResolutionZ );
		lClearVolumeRenderer.requestDisplay();

		while ( lClearVolumeRenderer.isShowing() )
		{
			try {
				Thread.sleep( 500 );
			} catch ( final InterruptedException e ) {
				e.printStackTrace();
			}
		}

		lClearVolumeRenderer.close();
	}
}

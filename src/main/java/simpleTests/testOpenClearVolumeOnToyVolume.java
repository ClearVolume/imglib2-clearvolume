package simpleTests;
/**
 *
 */


import java.nio.ByteBuffer;

import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
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
		goImgLibClearVolumeUnsignedShortType();
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

		ClearVolume.showClearVolumeUnsignedShortTypeImg( ( ArrayImg< ClearVolumeUnsignedShortType, ByteArray > ) imgVolumeDataArray, "Img -> ClearVolume", 512, 512, 512, 512 );
	}

	private static void goImgLibUnsignedShortType() {

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

		ClearVolume.showUnsignedShortTypeImg( imgVolumeDataArray, "Img -> ClearVolume", 512, 512, 512, 512 );
	}

	private static void goImgLibByteType() {

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

		ClearVolume.showByteTypeImg( imgVolumeDataArray, "Img -> ClearVolume", 512, 512, 512, 512 );
	}

	public static void goBasicStyle() {

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

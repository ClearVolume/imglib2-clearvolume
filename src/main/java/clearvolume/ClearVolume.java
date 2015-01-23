/**
 *
 */
package clearvolume;

import java.nio.ByteBuffer;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import clearvolume.renderer.ClearVolumeRendererInterface;
import clearvolume.renderer.factory.ClearVolumeRendererFactory;
import clearvolume.transferf.TransferFunctions;


/**
 * @author jug
 */
public class ClearVolume {

	/**
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 */
	public static void showByteTypeImg(
			final ArrayImg< ByteType, ByteArray > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight ) {

		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				1,
				pMaxTextureWidth,
				pMaxTextureHeight,
				1);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		// get the byte array out of the Img<ByteArray>
		final byte[] bytes = ( byte[] ) ( ( ArrayDataAccess< ? > ) imgVolumeDataArray.update( null ) ).getCurrentStorageArray();

		lClearVolumeRenderer.setCurrentRenderLayer(0);
		lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( bytes ),
				imgVolumeDataArray.dimension( 0 ),
				imgVolumeDataArray.dimension( 1 ),
				imgVolumeDataArray.dimension( 2 ));
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

	/**
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 */
	public static void showUnsignedShortTypeImg(
			final ArrayImg< UnsignedShortType, ShortArray > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight ) {

		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				2,
				pMaxTextureWidth,
				pMaxTextureHeight,
				1);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		// get the byte array out of the Img<ByteArray>
		final short[] shorts = ( short[] ) ( ( ArrayDataAccess< ? > ) imgVolumeDataArray.update( null ) ).getCurrentStorageArray();
		final byte[] bytes = new byte[ shorts.length * 2 ];

		int i = 0;
		for ( final short s : shorts ) {
			bytes[ i ] = ( byte ) ( s & 0xff );
			bytes[ i + 1 ] = ( byte ) ( ( s >> 8 ) & 0xff );
			i += 2;
		}

		lClearVolumeRenderer.setCurrentRenderLayer(0);
		lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( bytes ),
				imgVolumeDataArray.dimension( 0 ),
				imgVolumeDataArray.dimension( 1 ),
				imgVolumeDataArray.dimension( 2 ));
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

	/**
	 * @param imgVolumeDataArray
	 * @param pWindowName
	 * @param pWindowWidth
	 * @param pWindowHeight
	 * @param pMaxTextureWidth
	 * @param pMaxTextureHeight
	 */
	public static void showClearVolumeUnsignedShortTypeImg(
			final ArrayImg< ClearVolumeUnsignedShortType, ByteArray > imgVolumeDataArray,
			final String pWindowName,
			final int pWindowWidth,
			final int pWindowHeight,
			final int pMaxTextureWidth,
			final int pMaxTextureHeight ) {
		final ClearVolumeRendererInterface lClearVolumeRenderer = ClearVolumeRendererFactory.newBestRenderer(
				pWindowName,
				pWindowWidth,
				pWindowHeight,
				2,
				pMaxTextureWidth,
				pMaxTextureHeight,
				1);
		lClearVolumeRenderer.setTransferFunction(TransferFunctions.getGrayLevel());
		lClearVolumeRenderer.setVisible(true);

		// get the byte array out of the Img<ByteArray>
		final byte[] bytes = ( byte[] ) ( ( ArrayDataAccess< ? > ) imgVolumeDataArray.update( null ) ).getCurrentStorageArray();

		lClearVolumeRenderer.setCurrentRenderLayer(0);
		lClearVolumeRenderer.setVolumeDataBuffer( ByteBuffer.wrap( bytes ),
				imgVolumeDataArray.dimension( 0 ),
				imgVolumeDataArray.dimension( 1 ),
				imgVolumeDataArray.dimension( 2 ));
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

/**
 *
 */
package de.mpicbg.jug.clearvolume;

import net.imglib2.img.NativeImg;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.basictypeaccess.ByteAccess;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Fraction;
import net.imglib2.util.Util;


/**
 * @author jug
 */
public class ClearVolumeUnsignedShortType implements RealType< ClearVolumeUnsignedShortType >, NativeType< ClearVolumeUnsignedShortType > {

	int i = 0;

	final protected NativeImg< ?, ? extends ByteAccess > img;

	// the DataAccess that holds the information
	protected ByteAccess dataAccess;

	// this is the constructor if you want it to read from an array
	public ClearVolumeUnsignedShortType( final NativeImg< ?, ? extends ByteAccess > byteStorage ) {
		img = byteStorage;
	}

	// this is the constructor if you want it to be a variable
	public ClearVolumeUnsignedShortType( final int value ) {
		img = null;
		dataAccess = new ByteArray( 2 );
		setValue( UnsignedShortType.getCodedSignedShortChecked( value ) );
	}

	// this is the constructor if you want to specify the dataAccess
	public ClearVolumeUnsignedShortType( final ByteAccess access ) {
		img = null;
		dataAccess = access;
	}

	// this is the constructor if you want it to be a variable
	public ClearVolumeUnsignedShortType() {
		this( ( short ) 0 );
	}

	@Override
	public Fraction getEntitiesPerPixel() {
		return new Fraction( 2, 1 );
	}

	@Override
	public void updateContainer( final Object c ) {
		dataAccess = img.update( c );
	}

	public int get() {
		return getUnsignedShort( getValue() );
	}

	public static int getUnsignedShort( final short signedShort ) {
		return signedShort & 0xffff;
	}

	protected short getValue() {
		final byte b1 = dataAccess.getValue( 2 * i );
		final byte b2 = dataAccess.getValue( 2 * i + 1 );
		return ( short ) ( b1 * 0xff + b2 );
	}

	public void set( final int f ) {
		setValue( getCodedSignedShort( f ) );
	}

	public static short getCodedSignedShort( final int unsignedShort ) {
		return ( short ) ( unsignedShort & 0xffff );
	}

	protected void setValue( final short f ) {
		dataAccess.setValue( 2*i, ( byte ) ( f & 0xff ) );
		dataAccess.setValue( 2 * i + 1, ( byte ) ( ( f >> 8 ) & 0xff ) );
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#getRealDouble()
	 */
	@Override
	public double getRealDouble() {
		return get();
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#getRealFloat()
	 */
	@Override
	public float getRealFloat() {
		return get();
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#getImaginaryDouble()
	 */
	@Override
	public double getImaginaryDouble() {
		return 0.;
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#getImaginaryFloat()
	 */
	@Override
	public float getImaginaryFloat() {
		return 0f;
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#setReal(float)
	 */
	@Override
	public void setReal( final float f ) {
		set( ( int ) f );
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#setReal(double)
	 */
	@Override
	public void setReal( final double f ) {
		set( ( int ) f );
	}

	/**
	 * Does not apply --> does nothing!
	 *
	 * @see net.imglib2.type.numeric.ComplexType#setImaginary(float)
	 */
	@Override
	public void setImaginary( final float f ) {
	}

	/**
	 * Does not apply --> does nothing!
	 *
	 * @see net.imglib2.type.numeric.ComplexType#setImaginary(double)
	 */
	@Override
	public void setImaginary( final double f ) {
	}

	/**
	 * Sets the real part, ignores imaginary component.
	 *
	 * @see net.imglib2.type.numeric.ComplexType#setComplexNumber(float, float)
	 */
	@Override
	public void setComplexNumber( final float r, final float i ) {
		set( ( int ) r );
	}

	/**
	 * Sets the real part, ignores imaginary component.
	 *
	 * @see net.imglib2.type.numeric.ComplexType#setComplexNumber(double,
	 *      double)
	 */
	@Override
	public void setComplexNumber( final double r, final double i ) {
		set( ( int ) r );
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#getPowerFloat()
	 */
	@Override
	public float getPowerFloat() {
		return Math.abs( getRealFloat() );
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#getPowerDouble()
	 */
	@Override
	public double getPowerDouble() {
		return Math.abs( getRealDouble() );
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#getPhaseFloat()
	 */
	@Override
	public float getPhaseFloat() {
		return 0;
	}

	/**
	 * @see net.imglib2.type.numeric.ComplexType#getPhaseDouble()
	 */
	@Override
	public double getPhaseDouble() {
		return 0;
	}

	/**
	 * Does not apply --> does nothing!
	 *
	 * @see net.imglib2.type.numeric.ComplexType#complexConjugate()
	 */
	@Override
	public void complexConjugate() {
	}

	/**
	 * @see net.imglib2.type.Type#createVariable()
	 */
	@Override
	public ClearVolumeUnsignedShortType createVariable() {
		return new ClearVolumeUnsignedShortType();
	}

	/**
	 * @see net.imglib2.type.Type#copy()
	 */
	@Override
	public ClearVolumeUnsignedShortType copy() {
		return new ClearVolumeUnsignedShortType( get() );
	}

	/**
	 * @see net.imglib2.type.Type#set(net.imglib2.type.Type)
	 */
	@Override
	public void set( final ClearVolumeUnsignedShortType c ) {
		this.set( c.get() );
	}

	/**
	 * @see net.imglib2.type.operators.Add#add(java.lang.Object)
	 */
	@Override
	public void add( final ClearVolumeUnsignedShortType c ) {
		this.set( get() + c.get() );
	}

	/**
	 * @see net.imglib2.type.operators.Mul#mul(java.lang.Object)
	 */
	@Override
	public void mul( final ClearVolumeUnsignedShortType c ) {
		this.set( get() * c.get() );
	}

	/**
	 * @see net.imglib2.type.operators.Sub#sub(java.lang.Object)
	 */
	@Override
	public void sub( final ClearVolumeUnsignedShortType c ) {
		this.set( get() - c.get() );
	}

	/**
	 * @see net.imglib2.type.operators.Div#div(java.lang.Object)
	 */
	@Override
	public void div( final ClearVolumeUnsignedShortType c ) {
		this.set( get() / c.get() );
	}

	/**
	 * @see net.imglib2.type.operators.SetOne#setOne()
	 */
	@Override
	public void setOne() {
		set( 1 );
	}

	/**
	 * @see net.imglib2.type.operators.SetZero#setZero()
	 */
	@Override
	public void setZero() {
		set( 0 );
	}

	/**
	 * @see net.imglib2.type.operators.MulFloatingPoint#mul(float)
	 */
	@Override
	public void mul( final float c ) {
		this.set( Util.round( get() * c ) );
	}

	/**
	 * @see net.imglib2.type.operators.MulFloatingPoint#mul(double)
	 */
	@Override
	public void mul( final double c ) {
		this.set( ( int ) Util.round( get() * c ) );
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo( final ClearVolumeUnsignedShortType o ) {
		return get() - o.get();
	}

	/**
	 * @see net.imglib2.type.NativeType#createSuitableNativeImg(net.imglib2.img.NativeImgFactory,
	 *      long[])
	 */
	@Override
	public NativeImg< ClearVolumeUnsignedShortType, ? > createSuitableNativeImg( final NativeImgFactory< ClearVolumeUnsignedShortType > storageFactory, final long[] dim ) {
		// create the container
		final NativeImg< ClearVolumeUnsignedShortType, ? extends ByteAccess > container = storageFactory.createByteInstance( dim, getEntitiesPerPixel() );

		// create a Type that is linked to the container
		final ClearVolumeUnsignedShortType linkedType = new ClearVolumeUnsignedShortType( container );

		// pass it to the NativeContainer
		container.setLinkedType( linkedType );

		return container;
	}

	/**
	 * @see net.imglib2.type.NativeType#duplicateTypeOnSameNativeImg()
	 */
	@Override
	public ClearVolumeUnsignedShortType duplicateTypeOnSameNativeImg() {
		return new ClearVolumeUnsignedShortType( img );
	}

	@Override
	public void updateIndex( final int index ) {
		i = index;
	}

	@Override
	public int getIndex() {
		return i;
	}

	@Override
	public void incIndex() {
		++i;
	}

	@Override
	public void incIndex( final int increment ) {
		i += increment;
	}

	@Override
	public void decIndex() {
		--i;
	}

	@Override
	public void decIndex( final int decrement ) {
		i -= decrement;
	}

	/**
	 * @see net.imglib2.type.numeric.RealType#inc()
	 */
	@Override
	public void inc() {
		set( get() + 1 );
	}

	/**
	 * @see net.imglib2.type.numeric.RealType#dec()
	 */
	@Override
	public void dec() {
		set( get() - 1 );
	}

	/**
	 * @see net.imglib2.type.numeric.RealType#getMaxValue()
	 */
	@Override
	public double getMaxValue() {
		return -Short.MIN_VALUE + Short.MAX_VALUE;
	}

	/**
	 * @see net.imglib2.type.numeric.RealType#getMinValue()
	 */
	@Override
	public double getMinValue() {
		return 0;
	}

	/**
	 * @see net.imglib2.type.numeric.RealType#getMinIncrement()
	 */
	@Override
	public double getMinIncrement() {
		return 1;
	}

	/**
	 * @see net.imglib2.type.numeric.RealType#getBitsPerPixel()
	 */
	@Override
	public int getBitsPerPixel() {
		return 16;
	}

}

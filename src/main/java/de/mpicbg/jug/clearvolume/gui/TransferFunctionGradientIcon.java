package de.mpicbg.jug.clearvolume.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import clearvolume.transferf.TransferFunction;

class TransferFunctionGradientIcon implements Icon {

	private final int width;
	private final int height;
	private final float[] colorArray;

	TransferFunctionGradientIcon( final int width, final int height, final TransferFunction tf ) {
		this.height = height;
		this.width = width;
		this.colorArray = tf.getArray();
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

	/**
	 * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
	 */
	@Override
	public void paintIcon( final Component c, final Graphics gr, final int x, final int y ) {
		final Graphics2D g2d = ( Graphics2D ) gr.create();

		final double spacePerColor = 4.0 * getIconWidth() / ( colorArray.length - 1 );

		int start = 0;
		int end;
		for ( int i = 4; i < colorArray.length; i += 4 ) {
			final float r = colorArray[ i ];
			final float g = colorArray[ i + 1 ];
			final float b = colorArray[ i + 2 ];
			final float a = colorArray[ i + 3 ];
			end = ( int ) spacePerColor * ( i / 4 + 1 );
			g2d.setColor( new Color( r, g, b, a ) );
			g2d.fillRect( x + start, y, end - start, getIconHeight() );
			start = end;
		}
	}
}
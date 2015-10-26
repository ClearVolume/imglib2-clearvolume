// a class that wraps a Rangeslider going from -1 to 1

package de.mpicbg.jug.clearvolume.gui.rangeslider;

public class ClipRangeSlider extends RangeSlider
{
	private final int NTICKS = 100;

	public ClipRangeSlider()
	{
		super();
		setMinimum(-NTICKS);
		setMaximum(NTICKS);
		setValueLower(-1.f);
		setValueUpper(1.f);
	}

	public float getValueLower()
	{
		return 1.f * getValue() / NTICKS;
	}

	public void setValueLower(float val)
	{
		setValue((int) (val * NTICKS));
	}

	public float getValueUpper()
	{
		return 1.f * getUpperValue() / NTICKS;
	}

	public void setValueUpper(float val)
	{
		setUpperValue((int) (val * NTICKS));
	}

}

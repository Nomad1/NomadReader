package net.runserver.textReader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.shapes.Shape;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class ColorMenu implements OnTouchListener, OnSeekBarChangeListener, OnClickListener
{
	private final View m_colorMenu;
	private final TextView m_textSample;
	private final SeekBar m_lightSlider;
	private final SeekBar m_hueSlider;
	private final SeekBar m_satSlider;

	private final Button m_radioText;
	private final Button m_radioBack;
	
	private final LayerDrawable m_satDrawable;
	private final LayerDrawable m_lightDrawable;
	
	private final float m_density; 

	private float [] m_textColor = new float[3];
	private float [] m_backColor = new float[3];
	private boolean m_inverse;
	
	public int getTextColor()
	{
		return Color.HSVToColor(m_textColor);
	}
	
	public int getBackColor()
	{
		return Color.HSVToColor(m_backColor);
	}
	
	public void setColors(int textColor, int backColor)
	{
		Color.colorToHSV(textColor, m_textColor);
		Color.colorToHSV(backColor, m_backColor);
		
		m_textSample.setBackgroundColor(backColor);
		m_textSample.setTextColor(textColor);
		
		if (!m_radioText.isEnabled())
			setColor(m_textColor);
		else
			setColor(m_backColor);
	}
	
	public void setInverse(boolean value)
	{
		if (m_inverse == value)
			return;
		m_inverse = value;
		float [] tmp = m_backColor;
		m_backColor = m_textColor;
		m_textColor = tmp;
		
		m_textSample.setBackgroundColor(Color.HSVToColor(m_backColor));
		m_textSample.setTextColor(Color.HSVToColor(m_textColor));
		
		if (!m_radioText.isEnabled())
			setColor(m_textColor);
		else
			setColor(m_backColor);
	}

	public ColorMenu(Context context, View colorMenu, int textColor, int backColor)
	{
		DisplayMetrics dm = new DisplayMetrics();
		((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		m_density = dm.density;
		
		m_colorMenu = colorMenu;

		m_textSample = (TextView) m_colorMenu.findViewById(R.id.color_sample);
		m_textSample.setBackgroundColor(backColor);
		m_textSample.setTextColor(textColor);

		Color.colorToHSV(textColor, m_textColor);
		Color.colorToHSV(backColor, m_backColor);

		
		m_lightSlider = (SeekBar) m_colorMenu.findViewById(R.id.light_slider);
		m_lightSlider.setOnSeekBarChangeListener(this);
		m_lightSlider.setThumb(context.getResources().getDrawable(R.drawable.scrubber_control_selector_holo));
		m_hueSlider = (SeekBar) m_colorMenu.findViewById(R.id.hue_slider);
		m_hueSlider.setOnSeekBarChangeListener(this);
		m_hueSlider.setThumb(context.getResources().getDrawable(R.drawable.scrubber_control_selector_holo));
		m_satSlider = (SeekBar) m_colorMenu.findViewById(R.id.sat_slider);
		m_satSlider.setOnSeekBarChangeListener(this);
		m_satSlider.setThumb(context.getResources().getDrawable(R.drawable.scrubber_control_selector_holo));

		m_radioText = (Button) m_colorMenu.findViewById(R.id.button_text);
		m_radioText.setOnClickListener(this);
		m_radioBack = (Button) m_colorMenu.findViewById(R.id.button_back);
		m_radioBack.setOnClickListener(this);
		
		ShapeDrawable hueDrawable = (ShapeDrawable) ((LayerDrawable)m_hueSlider.getProgressDrawable()).findDrawableByLayerId(android.R.id.background);		
		CustomShapeDrawable hueCustomShape = new CustomShapeDrawable(hueDrawable.getShape());
		hueCustomShape.setBitmap(((BitmapDrawable)context.getResources().getDrawable(R.drawable.color_hue_image)).getBitmap());
		((LayerDrawable)m_hueSlider.getProgressDrawable()).setDrawableByLayerId(android.R.id.background, hueCustomShape);
		
		/*ShapeDrawable lightDrawable = (ShapeDrawable) ((LayerDrawable)m_lightSlider.getProgressDrawable()).findDrawableByLayerId(android.R.id.background);		
		CustomShapeDrawable lightCustomShape = new CustomShapeDrawable(lightDrawable.getShape());
		lightCustomShape.setBitmap(((BitmapDrawable)context.getResources().getDrawable(R.drawable.color_light_image)).getBitmap());
		((LayerDrawable)m_lightSlider.getProgressDrawable()).setDrawableByLayerId(android.R.id.background, lightCustomShape);*/
		
		m_satDrawable = ((LayerDrawable)m_satSlider.getProgressDrawable());
		m_lightDrawable = ((LayerDrawable)m_lightSlider.getProgressDrawable());
		
		
		setColor(m_textColor);
		
		//m_colorMenu.add
	}

	private void setColor(float[] hsv)
	{
		m_hueSlider.setProgress((int) (hsv[0] * m_hueSlider.getMax()/360));
		m_satSlider.setProgress((int) (hsv[1] * m_satSlider.getMax()));
		m_lightSlider.setProgress((int) (hsv[2] * m_lightSlider.getMax()));
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1)
	{
		return false;
	}

	@Override
	public void onProgressChanged(SeekBar slider, int value, boolean arg2)
	{
		float[] hsv = new float[3];
		
		hsv[2] = (m_lightSlider.getProgress() / (float)m_lightSlider.getMax());
		hsv[1] = (m_satSlider.getProgress() / (float)m_satSlider.getMax());
		hsv[0] = (360*m_hueSlider.getProgress() / (float)m_hueSlider.getMax());

		if (slider != m_satSlider)
		{
			float v = hsv[2];
			//if (v < 0.1f)
				//v = 0.1f;
			int fromColor = Color.HSVToColor(new float[]{hsv[0], 0, v});
			int toColor = Color.HSVToColor(new float[]{hsv[0], 1, v});
			
			Rect bounds = m_satDrawable.findDrawableByLayerId(android.R.id.background).getBounds();			
			GradientDrawable gradient = new GradientDrawable(Orientation.LEFT_RIGHT, new int[]{fromColor, toColor});
			gradient.setBounds(bounds);
			gradient.setDither(true);
			gradient.setCornerRadius(m_density*2);
			gradient.setStroke((int)m_density, 0xffaaaaaa);
			m_satDrawable.setDrawableByLayerId(android.R.id.background, gradient);
			m_satSlider.invalidate();
		}
		
		if (slider != m_lightSlider)
		{
			float s = hsv[1];
			//if (s < 0.1f)
				//s = 0.1f;
			int fromColor = Color.HSVToColor(new float[]{hsv[0], s, 0});
			int toColor = Color.HSVToColor(new float[]{hsv[0], s, 1});
			
			Rect bounds = m_lightDrawable.findDrawableByLayerId(android.R.id.background).getBounds();			
			GradientDrawable gradient = new GradientDrawable(Orientation.LEFT_RIGHT, new int[]{fromColor, toColor});
			gradient.setBounds(bounds);
			gradient.setDither(true);
			gradient.setCornerRadius(m_density*2);
			gradient.setStroke((int)m_density, 0xffaaaaaa);
			m_lightDrawable.setDrawableByLayerId(android.R.id.background, gradient);
			m_lightSlider.invalidate();
		}

		int color = Color.HSVToColor(hsv);
		
		if (!m_radioText.isEnabled())
		{
			m_textColor = hsv;
			m_textSample.setTextColor(color);
		} else
		{
			m_backColor = hsv;
			m_textSample.setBackgroundColor(color);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar slider)
	{
	}

	@Override
	public void onStopTrackingTouch(SeekBar slider)
	{
	}

	@Override
	public void onClick(View view)
	{
		if (view == m_radioText)
		{
			m_radioText.setEnabled(false);
			m_radioBack.setEnabled(true);
			setColor(m_textColor);
		} else
		{
			m_radioBack.setEnabled(false);
			m_radioText.setEnabled(true);
			setColor(m_backColor);
		}
	}
	
	class CustomShapeDrawable extends ShapeDrawable
	{
		private Bitmap m_bitmap;
		
		public void setBitmap(Bitmap bitmap)
		{
			m_bitmap = bitmap;
		}
		
		public Bitmap getBitmap()
		{
			return m_bitmap;
		}
		
		public CustomShapeDrawable(Shape shape)
		{
			super(shape);
		}
		
		@Override
		protected void onBoundsChange(Rect bounds)
		{			
			super.onBoundsChange(bounds);
			
			if (m_bitmap != null)
			{
				Bitmap bitmap = Bitmap.createScaledBitmap(m_bitmap, bounds.width(), bounds.height(), true);		
				BitmapShader shader = new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);				
				getPaint().setShader(shader);
			}
		}
	}	
}

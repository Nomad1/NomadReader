package net.runserver.library;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.content.res.Resources.NotFoundException;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

public class CustomScaleAnimation extends Animation
{
	private View m_view;
	private int m_initialWidth;
	private int m_initialHeight;
	private int m_lastWidth;
	private int m_lastHeight;
	private float m_deltaWidth;
	private float m_deltaHeight;
	private float m_lastTime;

	public void setView(View view, float scaleXPercent, float scaleYPercent)
	{
		m_view = view;
		MarginLayoutParams params = (MarginLayoutParams)m_view.getLayoutParams();
		m_initialWidth = view.getWidth() + params.leftMargin + params.rightMargin;
		m_initialHeight = view.getHeight() + params.topMargin + params.bottomMargin;
		m_deltaWidth = m_initialWidth * (scaleXPercent - 1.0f);
		m_deltaHeight = m_initialHeight * (scaleYPercent - 1.0f); 
		m_lastTime = 0;
	}

	public CustomScaleAnimation(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	protected void applyTransformation(float interpolatedTime, Transformation t)
	{
		if (m_view != null && interpolatedTime - m_lastTime > 0.05f)
		{
			int newWidth = m_initialWidth + (int) Math.round(m_deltaWidth * interpolatedTime);
			int newHeight = m_initialHeight + (int) Math.round(m_deltaHeight * interpolatedTime);			
			m_lastTime = interpolatedTime; 			
			
			if (newWidth != m_lastWidth || newHeight != m_lastHeight)
			{
				MarginLayoutParams params = (MarginLayoutParams)m_view.getLayoutParams();
				//params.setMargins((newWidth - params.width)/2, (newHeight - params.height)/2, (newWidth - params.width)/2, (newHeight - params.height)/2);
				params.width = newWidth;
				params.height = newHeight;
				m_view.setLayoutParams(params);
			}
		}
		
		//super.applyTransformation(interpolatedTime, t);		
	}

	public static Animation loadAnimation(Context context, int id) throws NotFoundException
	{
		XmlResourceParser parser = null;
		try
		{
			parser = context.getResources().getAnimation(id);
			return createAnimationFromXml(context, parser, null, Xml.asAttributeSet(parser));
		}
		catch (XmlPullParserException ex)
		{
			NotFoundException rnf = new NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		}
		catch (IOException ex)
		{
			NotFoundException rnf = new NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		}
		finally
		{
			if (parser != null)
				parser.close();
		}
	}
	
	private static Animation createAnimationFromXml(Context c, XmlPullParser parser, AnimationSet parent, AttributeSet attrs)
			throws XmlPullParserException, IOException
	{

		Animation anim = null;

		// Make sure we are on a start tag.
		int type;
		int depth = parser.getDepth();

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT)
		{

			if (type != XmlPullParser.START_TAG)
			{
				continue;
			}

			String name = parser.getName();

			if (name.equals("set"))
			{
				anim = new AnimationSet(c, attrs);
				createAnimationFromXml(c, parser, (AnimationSet) anim, attrs);
			} else if (name.equals("alpha"))
			{
				anim = new AlphaAnimation(c, attrs);
			} else if (name.equals("scale"))
			{
				anim = new ScaleAnimation(c, attrs);
			} else if (name.equals("custom_scale"))
			{
				anim = new CustomScaleAnimation(c, attrs);
			} else if (name.equals("rotate"))
			{
				anim = new RotateAnimation(c, attrs);
			} else if (name.equals("translate"))
			{
				anim = new TranslateAnimation(c, attrs);
			} else
			{
				throw new RuntimeException("Unknown animation name: " + parser.getName());
			}

			if (parent != null)
			{
				parent.addAnimation(anim);
			}
		}
		return anim;
	}
}

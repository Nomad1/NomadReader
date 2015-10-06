package net.runserver.common;

public class Base64InputStream 
//extends InputStream
{
	private static final String s_base64String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
	public static final byte[] s_charMap = new byte[128];
	
	static
	{
		for (int i = 0; i < s_charMap.length; i++)
			s_charMap[i] = -1;
		
		for (int i = 0; i < 64; i++)
			s_charMap[s_base64String.charAt(i)] = (byte)i;
		
		s_charMap['='] = s_charMap['A'];
	}
	/*
	
	private final byte[] m_buffer = new byte[3];
	private final byte[] m_block = new byte[4];
	private InputStream m_inputStream;
	private int m_position;
	private boolean m_finished;

	public Base64InputStream(InputStream inputStream)
	{
		m_inputStream = inputStream;
		m_position = 3;
		m_finished = false;
	}

	@Override
	public int available() throws IOException
	{
		return m_finished || m_inputStream.available() == 0 ? 0 : 3;//m_inputStream.available() * 3 / 4;
	}

	public int read(final byte[] b, final int offset, int length) throws IOException
	{
		if (m_finished)
			return -1;

		for (int i = 0; i < length; i += 3)
		{
			if (i + offset + 2 > b.length || !advance(b, offset + i))
				return i;
		}

		return length;
	}

	@Override
	public int read() throws IOException
	{
		if (m_position == 3)
		{
			if (!advance(m_buffer, 0))
			{
				m_finished = true;
				return 0;
			}
		}

		return m_buffer[m_position++];
	}

	private boolean advance(final byte[] bb, int offset) throws IOException
	{
		m_position = 0;		
		
		for(int i=0;i<m_block.length;i++)
			m_block[i] = 'A';
		
		for(int i=0;i<m_block.length;i++)
		{
			byte b;
			
			do
			{
				if (m_inputStream.available() < 1)
					return false;
				
				b = (byte)(m_inputStream.read()&0xFF);
			}
			while (b == '\r' || b== '\n' || b == ' ');
			
			if (b <= 0 || b > 127)
			{
				//result = false;
				//break;
			
				return false;
			}
			
			b = s_charMap[b];
			
			if (b == -1)
			{
				//result = false;
				//break;
				return false;
			}			

			m_block[i] = b;
		}
		
		//if (result || m_block[0] !='A')
		{ 
			bb[offset] = (byte) ((m_block[0] << 2) | (m_block[1] >>> 4));
			bb[offset + 1] = (byte) (((m_block[1] & 0xf) << 4) | (m_block[2] >>> 2));
			bb[offset + 2] = (byte) (((m_block[2] & 3) << 6) | m_block[3]);
		}

		return true;
	}*/

	public static byte[] processBase64(byte [] data, int offset, int length)
	{
		int [] resultLength = { 0 };
		return 	processBase64(data, offset, length, resultLength);	
	}

	public static byte[] processBase64(byte [] data, int offset, int length, int [] resultLength)
	{
		byte [] result = new byte[length*3/4];
		byte [] block = new byte[4];
		
		int roffset = 0;
		int i = offset;
		length += offset;
		int j = 0;
		
		while(i < length)
		{
			byte b = data[i++];
			
			if (b == '\r' || b== '\n' || b == ' ')
				continue;
			
			if (b <= 0 || b > 127 || s_charMap[b] == -1)
			{
				//Log.d("TextReader", "Invalid byte in input stream " + b);
				return roffset == 0 ? null : result;
			}
			
			block[j++] = s_charMap[b];
			
			if (j == 4 || i == length)
			{
				result[roffset] = (byte) ((block[0] << 2) | (block[1] >>> 4));
				result[roffset + 1] = (byte) (((block[1] & 0xf) << 4) | (block[2] >>> 2));
				result[roffset + 2] = (byte) (((block[2] & 3) << 6) | block[3]);
				roffset += 3;
				resultLength[0] = roffset;
				
				block[0] = 'A';
				block[1] = 'A';
				block[2] = 'A';
				block[3] = 'A';
				j = 0;
			}			
		}
		return result;
	}
}

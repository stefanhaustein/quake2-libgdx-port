/*
Copyright (C) 1997-2001 Id Software, Inc.

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  

See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/
/* Modifications
   Copyright 2003-2004 Bytonic Software
   Copyright 2010 Google Inc.
*/
package com.googlecode.gdxquake2.game.game;


import java.io.IOException;

import com.googlecode.gdxquake2.game.game.adapters.EntityThinkAdapter;
import com.googlecode.gdxquake2.game.util.QuakeFile;

public class MonsterMove {
	public MonsterMove(int firstframe, int lastframe, Frame frame[], EntityThinkAdapter endfunc) {
		
		this.firstframe= firstframe;
		this.lastframe= lastframe;
		this.frame= frame;
		this.endfunc= endfunc;
	}

	public MonsterMove()
	{}

	public int firstframe;
	public int lastframe;
	public Frame frame[]; //ptr
	public EntityThinkAdapter endfunc;
	

	/** Writes the structure to a random acccess file. */
	public void write(QuakeFile f) throws IOException
	{
		f.writeInt(firstframe);
		f.writeInt(lastframe);
		if (frame == null)
			f.writeInt(-1);
		else 
		{
			f. writeInt(frame.length);
			for (int n=0; n < frame.length; n++)
				frame[n].write(f);
		}
		f.writeAdapter(endfunc);
	}
	
	/** Read the mmove_t from the RandomAccessFile. */
	public void read(QuakeFile f) throws IOException
	{
		firstframe = f.readInt();
		lastframe = f.readInt();
		
		int len = f.readInt();
		
		frame = new Frame[len];
		for (int n=0; n < len ; n++)
		{			
			frame[n] = new Frame();
			frame[n].read(f);
		}
		endfunc = (EntityThinkAdapter) f.readAdapter();
	}
}

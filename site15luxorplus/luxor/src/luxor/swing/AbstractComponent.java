/*
** Luxor - XML User Interface Language (XUL) Toolkit
** Copyright (c) 2001, 2002, 2003 by Gerald Bauer
**
** This program is free software.
**
** You may redistribute it and/or modify it under the terms of the GNU
** General Public License as published by the Free Software Foundation.
** Version 2 of the license should be included with this distribution in
** the file LICENSE, as well as License.html. If the license is not
** included with this distribution, you may find a copy at the FSF web
** site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
** Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
**
** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
** REDISTRIBUTION OF THIS SOFTWARE.
**
*/

package luxor.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import luxor.core.*;
import luxor.spi.*;
import luxor.spi.resolver.*;
import houston.*;

public abstract class AbstractComponent implements NComponent
{
   static Logger T = Logger.getLogger( AbstractComponent.class );

   private XulTag _def;

   public AbstractComponent( XulTag def )
   {
      _def = def;
   }

   public void setBackgroundColor( JComponent comp )
   {
      Color backgroundColor = _def.getBackgroundColor();
      if( backgroundColor != null )
      {
         // check if component is transparent (aka non-opaque)
         if( !comp.isOpaque() )
            comp.setOpaque( true );

         comp.setBackground( backgroundColor );
      }
   }

   public void setBorder( JComponent comp )
   {
      Border border = _def.getBorder();
      if( border != null )
         comp.setBorder( border );
   }

   public void setColor( JComponent comp )
   {
      Color color = _def.getColor();
      if( color != null )
         comp.setForeground( color );
   }

   public void setContextPopup( JComponent comp, PopupResolver resolver )
   {
      JPopupMenu contextPopup = _def.getContextPopup( resolver );

      if( contextPopup != null )
         comp.addMouseListener( new PopupMouseListener( comp, contextPopup ) );
   }

   public void setFont( JComponent comp )
   {
      Font font = _def.getFont();
      if( font != null )
         comp.setFont( font );
   }

   public void setName( JComponent comp )
   {
      comp.setName( _def.getId() );
   }

   public int getFlex()
   {
      return _def.getFlex();
   }

   public int getHeight()
   {
      return _def.getHeight();
   }

   public String getId()
   {
      return _def.getId();
   }

   public abstract JComponent getJComponent();

   public int getMaxHeight()
   {
      return _def.getMaxHeight();
   }

   public int getMaxWidth()
   {
      return _def.getMaxWidth();   // Skylight: was getMaxHeight(), so max-width never worked
   }

   public int getMinHeight()
   {
      return _def.getMinHeight();
   }

   public int getMinWidth()
   {
      return _def.getMinWidth();
   }

   public int getWidth()
   {
      return _def.getWidth();
   }

   public abstract boolean useMinHeight();

   public abstract boolean useMinWidth();
}

// todo: move class to salsa package
class PopupMouseListener implements MouseListener
{

   JComponent _parent;
   JPopupMenu _popup;

   public PopupMouseListener( JComponent parent, JPopupMenu popup )
   {
      _parent = parent;
      _popup = popup;
   }

   public void mouseClicked( MouseEvent ev )
   {
      checkPopup( ev );
   }

   public void mouseEntered( MouseEvent ev ) { }

   public void mouseExited( MouseEvent ev ) { }

   public void mousePressed( MouseEvent ev )
   {
      checkPopup( ev );
   }

   public void mouseReleased( MouseEvent ev )
   {
      checkPopup( ev );
   }

   private void checkPopup( MouseEvent ev )
   {
      if( ev.isPopupTrigger() )
         _popup.show( _parent, ev.getX(), ev.getY() );
   }
}

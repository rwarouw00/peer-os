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

package luxor.swing.input;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import luxor.core.*;
import luxor.core.input.*;
import luxor.spi.*;
import luxor.swing.*;
import luxor.swing.misc.*;
import caramel.util.*;
import houston.*;

public class XLabel extends AbstractInput
{
   static Logger T = Logger.getLogger( XLabel.class );

   private JLabel _label;

   public XLabel( LabelDef def )
   {
      super( def );

      // make sure getMinimumSize == getPreferredSize
      //  - this is needed for GridBagLayout
      //    so that the component doesn't get shrunk invisible

      _label =
         new JLabel()
         {
            public Dimension getMinimumSize()
            {
               return getPreferredSize();
            }
         };

      String value = "";

      // check if label has mixed content
      List content = def.getElement().getContent();

      if( content.size() != 0 )
      {
         // todo: should i check if content has tags
         //   and add html enclosing only if tags are present
         //   and not for text only content?

         value = "<html>"
                + HtmlOutputter.getContent( content )
                + "</html>";
      }
      else
      {
         // no content; use label's value attribute

         value = def.getValue();
         if( value == null )
            value = "";

         setFont( _label );
         setColor( _label );
         setBackgroundColor( _label );

         _label.setHorizontalAlignment( def.getAlign( _label.getHorizontalAlignment() ) );
         _label.setVerticalAlignment( def.getVerticalAlign( _label.getVerticalAlignment() ) );
      }

      String observes = def.getAttribute( Xul.Attribute.OBSERVES );
      if( observes != null )
      {
         // todo: check for hint
         HintPublisher.getHintPublisher().subscribe( _label );
      }

      _label.setLocation( def.getLeft( 0 ), def.getTop( 0 ) );

      setName( _label );
      setBorder( _label );   // Skylight: so padding, margin and border work on labels too

      _label.setText( value );

      String tooltip = def.getTooltip();
      if( tooltip != null )
         _label.setToolTipText( tooltip );
   }

   public void setText( String text )
   {
      _label.setText( text );
   }

   public JComponent getJComponent()
   {
      return _label;
   }

   public JLabel getLabel()
   {
      return null;
   }

   public String getText()
   {
      return _label.getText();
   }

   public boolean isEditable()
   {
      return true;
   }

   public void addFocusListener( FocusListener l )
   {
      // for now label has no focus/key board input
   }

   public void removeFocusListener( FocusListener l ) { }

   public void resolveReferences( NComponentResolver resolver ) { }

   public boolean useMinHeight()
   {
      return true;
   }

   public boolean useMinWidth()
   {
      return true;
   }
}

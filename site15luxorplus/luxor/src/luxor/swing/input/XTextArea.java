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

import java.awt.Font;
import java.awt.event.*;
import javax.swing.*;
import luxor.core.input.*;
import luxor.spi.*;
import luxor.swing.*;

public class XTextArea extends AbstractInput
{
   private JScrollPane _component;
   private JTextArea _textArea;

   public XTextArea( TextAreaDef def )
   {
      super( def );

      int rows = def.getRows( 3 );
      int cols = def.getCols( 10 );

      _textArea = new JTextArea( def.getValue(), rows, cols );

      setFont( _textArea );
      setColor( _textArea );
      setBackgroundColor( _textArea );

      // Skylight: named after its id like other controls, and wrap="true" and
      //  readonly="true" attributes
      setName( _textArea );
      if( def.getBooleanAttribute( "wrap", false ) )
      {
         _textArea.setLineWrap( true );
         _textArea.setWrapStyleWord( true );
      }
      if( def.getBooleanAttribute( "readonly", false ) )
         _textArea.setEditable( false );

      // wrap textArea into a scroll pane
      _component = new JScrollPane( _textArea );
   }

   public void setText( String text )
   {
      _textArea.setText( text );
   }

   public JComponent getJComponent()
   {
      return _component;
   }

   public JLabel getLabel()
   {
      return null;
   }

   public String getText()
   {
      return _textArea.getText();
   }

   public boolean isEditable()
   {
      return true;
   }

   public void addFocusListener( FocusListener l )
   {
      _textArea.addFocusListener( l );
   }

   public void removeFocusListener( FocusListener l )
   {
      _textArea.removeFocusListener( l );
   }

   public void resolveReferences( NComponentResolver resolver ) { }

   public boolean useMinHeight()
   {
      return false;
   }

   public boolean useMinWidth()
   {
      return false;
   }
}

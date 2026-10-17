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
import java.awt.Font;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import luxor.core.*;
import luxor.core.input.*;
import luxor.spi.*;
import luxor.spi.resolver.*;
import luxor.swing.*;
import luxor.swing.action.*;
import houston.*;
import luxor.*;

public class XButton extends AbstractInput
{
   static Logger T = Logger.getLogger( XButton.class );

   private AbstractButton _button;

   public XButton( ButtonDef def, XulContext ctx )
   {
      super( def );

      T.debug( "create XButton" );

      int type = def.getType();
      if( type == Xul.Type.RADIO || type == Xul.Type.CHECKBOX )
      {
         _button = new JToggleButton();

         if( def.getChecked() )
            _button.setSelected( true );

         if( type == Xul.Type.RADIO )
         {
            ButtonGroup group = def.getGroup( ctx );
            if( group != null )
               group.add( _button );
            else
               Xul.warning( "button of type radio requires group attribute" );
            // todo: add id, etc.
         }
      }
      else
         _button = new JButton();

      setFont( _button );
      setColor( _button );
      setBackgroundColor( _button );
      setName( _button );

      String label = def.getLabel();

      // Skylight: a button without a label (a picture button) has no text,
      //  instead of showing its id
      if( label == null )
         label = "";

      T.debug( "  label=" + label );

      _button.setText( label );

      // Skylight: tooltip="..." (Luxor documented it, but buttons ignored it)
      String tooltip = def.getTooltip();
      if( tooltip != null )
         _button.setToolTipText( tooltip );

      ImageIcon icon = def.getIcon( ctx );
      if( icon != null )
         _button.setIcon( icon );

      _button.setLocation( def.getLeft( 0 ), def.getTop( 0 ) );

      String href = def.getAttribute( Xul.Attribute.HREF );

      if( href != null )
      {
         String target = def.getAttribute( Xul.Attribute.TARGET );

         _button.addActionListener( new CmdShowDocument( href, target ) );
      }
      else
      {
         Action action = def.getAction( ctx );
         if( action != null )
            _button.addActionListener( action );
      }

// begin debug
      String actionKey = def.getAttribute( Xul.Attribute.COMMAND );
      if( actionKey == null )
         actionKey = "!no_command_found";
      T.debug( "  command=" + actionKey );
// end debug

      char accessKey = def.getAccessKey();
      if( accessKey != 0 )
         _button.setMnemonic( accessKey );
   }

   public void setText( String text )
   {
      _button.setText( text );
   }

   public JComponent getJComponent()
   {
      return _button;
   }

   public JLabel getLabel()
   {
      return null;
   }

   public String getText()
   {
      return _button.getText();
   }

   public boolean isEditable()
   {
      return false;
   }

   public void addFocusListener( FocusListener l )
   {
      // use click; button doesn't support key board input
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

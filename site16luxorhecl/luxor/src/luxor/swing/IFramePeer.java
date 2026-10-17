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

import java.awt.Component;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import luxor.core.*;
import luxor.core.widget.*;
import luxor.spi.*;
import luxor.spi.resolver.*;
import caramel.util.*;
import houston.*;
import luxor.*;

// todo: use plug-able browser widget
//   instead of JEditorPane
//  move all browser widget code out of IFrame into a new widget


public class IFramePeer extends AbstractComponent
       implements BrowserService
{
   static Logger T = Logger.getLogger( IFramePeer.class );

   private JEditorPane _browser;
   private JScrollPane _comp;

   public IFramePeer( IFrameDef def, XulContext ctx )
   {
      super( def );

      _browser = new JEditorPane();
      _browser.setEditable( false );
      _browser.setContentType( "text/html" );
      setName( _browser );   // Skylight: so scripts can find it by its id

      // add browser window/widget to browser registry
      //  use id as target
      ctx.addBrowser( def.getId(), this );

      String src = def.getAttribute( Xul.Attribute.SRC );

      if( src == null )
      {
         // Skylight: no src is an empty page, for scripts to fill with setPage
      }
      else
      {
         try
         {
            String html = def.loadText( ctx, src );
            _browser.setText( html );
         }
         catch( IOException ioex )
         {
            Xul.error( "failed to retrieve html document '" + src + "' for iframe: " + ioex.toString() );
         }
      }

      _comp = new JScrollPane( _browser );
   }

   public JComponent getJComponent()
   {
      return _comp;
   }

   public void resolveReferences( NComponentResolver resolver ) { }

   public void showDocument( URL url )
   {
      Status.info( "calling iframe.showDocument( " + url.toString() + " )" );
      try
      {
         _browser.setPage( url );
      }
      catch( IOException ioex )
      {
         Status.error( "*** failed to load document '" + url.toString() + "': " + ioex.toString() );
      }
   }

   public boolean useMinHeight()
   {
      return false;
   }

   public boolean useMinWidth()
   {
      return false;
   }
}

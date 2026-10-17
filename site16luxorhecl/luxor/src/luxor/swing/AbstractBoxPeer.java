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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import luxor.core.*;
import luxor.spi.*;
import luxor.spi.resolver.*;
import houston.*;

public abstract class AbstractBoxPeer extends AbstractContainer
{
   static Logger T = Logger.getLogger( AbstractBoxPeer.class );

   private XulTag _boxDef;   // Skylight: for the spacing attribute

   public AbstractBoxPeer( XulTag def )
   {
      super( def );
      _boxDef = def;
   }

   protected void setup( JPanel panel, int orient, List compDefs, JComponentResolver resolver, XulContext ctx )
   {
      if( orient == SwingConstants.VERTICAL )
         // top-to-bottom
         panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );
      else
         // left-to-right
         panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );

      int maxWidth = 0;
      int maxHeight = 0;
      int flexTotal = 0;

      ArrayList comps = new ArrayList();

      Iterator defIt = compDefs.iterator();
      while( defIt.hasNext() )
      {
         NComponentFactory f = ( NComponentFactory ) defIt.next();
         NComponent comp = f.createNComponent( resolver, ctx );

         comps.add( comp );

         flexTotal += comp.getFlex();

         JComponent jcomp = comp.getJComponent();

         // calculate max width or height depending on the
         //  orientation of the box
         if( orient == SwingConstants.VERTICAL )
         {
            Dimension max = jcomp.getMaximumSize();
            maxWidth = Math.max( maxWidth, max.width );
         }
         else
         {
            Dimension max = jcomp.getMaximumSize();
            maxHeight = Math.max( maxHeight, max.height );
         }
      }

      Iterator compIt = comps.iterator();
      int spacing = _boxDef.getIntAttribute( "spacing", 0 );   // Skylight
      while( compIt.hasNext() )
      {
         final NComponent comp = ( NComponent ) compIt.next();
         final JComponent jcomp = comp.getJComponent();
         final int finalOrient = orient;
         final int finalMaxWidth = maxWidth;
         final int finalMaxHeight = maxHeight;
         final int finalFlexTotal = flexTotal;

         fit( comp, jcomp, orient, maxWidth, maxHeight, flexTotal );

         // Skylight: every child is named after its id, so code can find any of them
         //  (Luxor named only some controls: not password fields, lists or boxes)
         String id = comp.getId();
         if( jcomp.getName() == null && id != null && !id.equals( "anonymous" ) )
            jcomp.setName( id );

         // Skylight: when a label's or button's text, icon or font changes, size it
         //  again, and the boxes around it (Luxor used to keep the size of a
         //  label's first text, cutting longer ones off with "...")
         jcomp.putClientProperty( REFIT, new Runnable()
         {
            public void run()
            {
               jcomp.invalidate();   // a box's layout keeps its children's old sizes until invalidated
               jcomp.setMinimumSize( null );
               jcomp.setPreferredSize( null );
               jcomp.setMaximumSize( null );
               // the box's width (or height) was taken from its children as they
               //  were built, so let this one have at least what it needs now
               Dimension need = jcomp.getPreferredSize();
               fit( comp, jcomp, finalOrient, Math.max( finalMaxWidth, need.width ),
                     Math.max( finalMaxHeight, need.height ), finalFlexTotal );
            }
         } );
         jcomp.addPropertyChangeListener( new java.beans.PropertyChangeListener()
         {
            public void propertyChange( java.beans.PropertyChangeEvent ev )
            {
               String name = ev.getPropertyName();
               if( "text".equals( name ) || "icon".equals( name ) || "font".equals( name ) )
                  refit( jcomp );
            }
         } );

         panel.add( jcomp );
         addComponent( comp );

         // Skylight: spacing="6" puts a gap between a box's children
         if( spacing > 0 && compIt.hasNext() )
         {
            if( orient == SwingConstants.VERTICAL )
               panel.add( Box.createVerticalStrut( spacing ) );
            else
               panel.add( Box.createHorizontalStrut( spacing ) );
         }
      }

      resolveReferences();

      setContextPopup( panel, ctx );
      setColor( panel );
      // todo: do I need to set opaque to true?
      setBackgroundColor( panel );

      if( XulDebug.debugLayout() )
      {
         T.debug( "flexTotal=" + flexTotal );

         Border debugBorder;

         if( orient == SwingConstants.VERTICAL )
         {
            // top-to-bottom

            debugBorder = BorderFactory.createLineBorder( Color.red, 3 );

            T.debug( "maxWidth=" + maxWidth );
         }
         else
         {
            // left-to-right

            debugBorder = BorderFactory.createLineBorder( Color.blue, 3 );

            T.debug( "maxHeight=" + maxHeight );
         }

         Border currentBorder = panel.getBorder();
         if( currentBorder == null )
            panel.setBorder( debugBorder );
         else
            panel.setBorder( new CompoundBorder( debugBorder, currentBorder ) );
      }
   }

   /** Skylight: the client property holding a child's "size me again" action. */
   private static final String REFIT = "skylight.refit";

   /**
    *  Skylight: sizes a component again, then each box around it that Luxor
    *  built, so a longer text makes room for itself all the way up.
    */
   static void refit( JComponent jcomp )
   {
      java.awt.Container c = jcomp;
      JComponent top = jcomp;
      while( c instanceof JComponent )
      {
         Object action = ( ( JComponent ) c ).getClientProperty( REFIT );
         if( action instanceof Runnable )
         {
            ( ( Runnable ) action ).run();
            top = ( JComponent ) c;
         }
         c = c.getParent();
      }
      java.awt.Container parent = top.getParent();
      if( parent instanceof JComponent )
      {
         ( ( JComponent ) parent ).revalidate();
         parent.repaint();
      }
   }

   /**
    *  Sets a child's minimum, preferred and maximum size for this box: Luxor's
    *  original rules, moved into a method (Skylight) so they can run again.
    */
   private void fit( NComponent comp, JComponent jcomp, int orient, int maxWidth, int maxHeight, int flexTotal )
   {
         Dimension min = jcomp.getMinimumSize();
      Dimension pref = jcomp.getPreferredSize();
      Dimension max = jcomp.getMaximumSize();

      if( orient == SwingConstants.VERTICAL )
      {
         // top-to-bottom

         jcomp.setAlignmentX( Component.LEFT_ALIGNMENT );

         // set maximum width to width of broadest component

         // check for flex(ibility) of height
         int flex = comp.getFlex();
         if( flex == 0 )
         {
            // min height == preferred height == max height

            if( XulDebug.debugLayout() )
            {
               String clazzName = jcomp.getClass().getName();
               T.debug( "[" + clazzName + "] min.height=" + min.height + ", min.width=" + min.width );
               T.debug( "[" + clazzName + "] pref.height=" + pref.height + ", pref.width=" + pref.width );
               T.debug( "[" + clazzName + "] max.height=" + max.height + ", max.width=" + max.width );
               T.debug( "[" + clazzName + "] maxWidth=" + maxWidth );
            }

            // check for height in xul def
            int height = comp.getHeight();
            if( height == -1 )
            {
               // no height defined; use component's preferred height

               if( comp.useMinHeight() )
               {
                  // preserve min.width, pref.width
                  min.setSize( min.width, pref.height );
                  pref.setSize( pref.width, pref.height );
                  max.setSize( maxWidth, pref.height );
               }
               else
               {
                  // preserve min, pref, max.height
                  max.setSize( maxWidth, max.height );
               }
            }
            else
            {
               // preserve min.width, pref.width
               min.setSize( min.width, height );
               pref.setSize( pref.width, height );
               max.setSize( maxWidth, height );
            }
            // Skylight: min-height and max-height apply here too
            int userMaxHeight = comp.getMaxHeight();
            if( userMaxHeight != -1 )
            {
               max.setSize( max.width, userMaxHeight );
               pref.setSize( pref.width, Math.min( pref.height, userMaxHeight ) );
               min.setSize( min.width, Math.min( min.height, userMaxHeight ) );
            }
            int userMinHeight = comp.getMinHeight();
            if( userMinHeight != -1 )
            {
               min.setSize( min.width, userMinHeight );
               pref.setSize( pref.width, Math.max( pref.height, userMinHeight ) );
            }
         }
         else
         {
            int minHeight = comp.getMinHeight();
            if( minHeight == -1 )
            {
               // preserve min.width
               if( comp.useMinHeight() )
                  min.setSize( min.width, pref.height );
            }
            else
            {
               // only set minHeight if supplied by user (in xul file)
               // preserve min.width
               min.setSize( min.width, minHeight );
            }

            // note: if maxHeight is set in xul
            //  we loose the proper flex setting
            //  as BoxLayout models the flex(ibility) using maxHeight
            int flexMaxHeight = comp.getMaxHeight();
            if( flexMaxHeight == -1 )
               flexMaxHeight = Integer.MAX_VALUE / flexTotal * flex;

            T.debug( "flexMaxHeight=" + flexMaxHeight );

            // todo: should I set pref.height using getHeight?

            // preserve pref
            // pref.setSize( pref.width, pref.height );
            max.setSize( maxWidth, flexMaxHeight );
         }
      }
      else
      {
         // left-to-right

         jcomp.setAlignmentY( Component.CENTER_ALIGNMENT );

         // set maximum height to height of highest component

         // check for flex(ibility) of height
         int flex = comp.getFlex();
         if( flex == 0 )
         {
            // min width == preferred width == max width

            if( XulDebug.debugLayout() )
            {
               String clazzName = jcomp.getClass().getName();
               T.debug( "[" + clazzName + "] min.width=" + min.width + ", min.height=" + min.height );
               T.debug( "[" + clazzName + "] pref.width=" + pref.width + ", pref.height=" + pref.height );
               T.debug( "[" + clazzName + "] max.width=" + max.width + ", max.height=" + max.height );
               T.debug( "[" + clazzName + "] maxHeight=" + maxHeight );
            }

            int width = comp.getWidth();
            if( width == -1 )
            {
               if( comp.useMinWidth() )
               {
                  // preserve min.height, pref.height
                  min.setSize( pref.width, min.height );
                  pref.setSize( pref.width, pref.height );
                  max.setSize( pref.width, maxHeight );
               }
               else
               {
                  // preserve min, pref, max.width
                  max.setSize( max.width, maxHeight );
               }
            }
            else
            {
               // preserve min.height, pref.height
               min.setSize( width, min.height );
               pref.setSize( width, pref.height );
               max.setSize( width, maxHeight );
            }
            // Skylight: min-width and max-width apply here too
            int userMaxWidth = comp.getMaxWidth();
            if( userMaxWidth != -1 )
            {
               max.setSize( userMaxWidth, max.height );
               pref.setSize( Math.min( pref.width, userMaxWidth ), pref.height );
               min.setSize( Math.min( min.width, userMaxWidth ), min.height );
            }
            int userMinWidth = comp.getMinWidth();
            if( userMinWidth != -1 )
            {
               min.setSize( userMinWidth, min.height );
               pref.setSize( Math.max( pref.width, userMinWidth ), pref.height );
            }
         }
         else
         {
            int minWidth = comp.getMinWidth();
            if( minWidth == -1 )
            {
               // preserve min.height
               if( comp.useMinWidth() )
                  min.setSize( pref.width, min.height );
            }
            else
            {
               // only set minWidth if supplied by user (in xul file)
               // preserve min.height
               min.setSize( minWidth, min.height );
            }

            // note: if maxWidth is set in xul
            //  we loose the proper flex setting
            //  as BoxLayout models the flex(ibility) using maxWidth
            int flexMaxWidth = comp.getMaxWidth();
            if( flexMaxWidth == -1 )
               flexMaxWidth = Integer.MAX_VALUE / flexTotal * flex;

            T.debug( "flexMaxWidth=" + flexMaxWidth );

            // todo: should I set pref.width using getWidth?

            // preserve pref
            // pref.setSize( pref.width, pref.height );
            max.setSize( flexMaxWidth, maxHeight );
         }
      }

      // Skylight: keep the sizes consistent, so a max-width or max-height also
      //  limits a flexible control's preferred size
      pref.setSize( Math.min( pref.width, max.width ), Math.min( pref.height, max.height ) );
      min.setSize( Math.min( min.width, pref.width ), Math.min( min.height, pref.height ) );
      jcomp.setMinimumSize( min );
      jcomp.setPreferredSize( pref );
      jcomp.setMaximumSize( max );

      // Skylight: a text field with cols="..." ignores the preferred size set
      //  above (Swing works it out from the columns), so drop the columns
      //  when the box has made it narrower
      if( jcomp instanceof JTextField && ( ( JTextField ) jcomp ).getColumns() > 0
             && jcomp.getPreferredSize().width > pref.width )
         ( ( JTextField ) jcomp ).setColumns( 0 );
   }
}

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

package luxor.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.jaxen.JaxenException;
import org.jaxen.XPathSyntaxException;
import org.jaxen.jdom.XPath;
import org.jdom.*;
import luxor.core.css.*;
import luxor.css.*;
import luxor.css.data.*;
import luxor.spi.*;
import luxor.spi.resolver.*;
import caramel.util.*;
import cypress.*;
import houston.*;
import luxor.*;

public class XulTag
{
   static Logger T = Logger.getLogger( XulTag.class );

   private Element _element;

   private String _id;

   /**
    *  cached style properties todo: use Style instead of StyleSheet?
    */
   private StyleSheet _style;
   private String _styleClass;

   public XulTag( Element element )
   {
      _element = element;

      // make sure required attribute id is always present
      _id = getAttribute( Xul.Attribute.ID, "anonymous" );

      _styleClass = getAttribute( Xul.Attribute.CLASS, "anonymous" );

      T.debug( "created " + element.getName() + ": id=" + _id );
   }

   public KeyStroke getAccelKey( KeyStrokeResolver resolver )
   {
      String accelKey = getAttribute( Xul.Attribute.KEY );
      if( accelKey == null )
         return null;

      return resolver.lookupKeyStroke( accelKey );
   }

   public char getAccessKey()
   {
      String accessKey = getAttribute( Xul.Attribute.ACCESSKEY );
      if( accessKey == null || accessKey.length() == 0 )
         return 0;

      return accessKey.charAt( 0 );
   }

   public int getAccessKeyIndex()
   {
      // returns -1 if no access key found
      return getIntAttribute( Xul.Attribute.ACCESSKEYINDEX, -1 );
   }


   public Action getAction( ActionResolver resolver )
   {
      String actionKey = getAttribute( Xul.Attribute.COMMAND );
      if( actionKey == null )
         return null;

      Action action = resolver.lookupAction( actionKey );
      if( action == null )
         Xul.Warning.REFERENCE_NOT_FOUND( "action", actionKey );

      return action;
   }

   public int getAlign( int defaultValue )
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.ALIGN );
      if( value == null )
         return defaultValue;

      if( value.getType() == CssValue.IDENT )
      {
         // todo: check why getType is IDENT and not STRING

         String stringValue = ( ( CssString ) value ).getStringValue();

         Css.TypeInfo typeInfo = Css.getTypeInfo( stringValue );

         if( typeInfo.getMajor() == Css.Type.ALIGN )
         {
            switch ( typeInfo.getMinor() )
            {
               case Css.Align.Id.LEFT:
                  return SwingConstants.LEFT;
               case Css.Align.Id.CENTER:
                  return SwingConstants.CENTER;
               case Css.Align.Id.RIGHT:
                  return SwingConstants.RIGHT;
               // fallthrough for default case
            }
         }

         Xul.SyntaxError.UNKNOWN_CSS_PROPERTY_VALUE( stringValue, Css.Property.ALIGN );
      }
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.ALIGN );
      }

      return defaultValue;
   }


   public int getAlignAttribute( String key, int defaultValue )
   {
      String value = getAttribute( key );

      if( value == null )
         return defaultValue;
      else
      {
         value = value.toLowerCase().trim();

         if( value.equals( "top" ) )
            return Xul.Align.TOP;
         else if( value.equals( "center" ) )
            return Xul.Align.CENTER;
         else if( value.equals( "bottom" ) )
            return Xul.Align.BOTTOM;
         else if( value.equals( "left" ) )
            return Xul.Align.LEFT;
         else if( value.equals( "right" ) )
            return Xul.Align.RIGHT;
         else if( value.equals( "max" ) )
            return Xul.Align.MAX;
         else if( value.equals( "middle" ) )
            return Xul.Align.MIDDLE;
         else
            return defaultValue;
      }
   }

   public String getAttribute( String key, String defaultValue )
   {
      String value = _element.getAttributeValue( key );
      if( value == null )
         return defaultValue;
      else
         return value;
   }

   public String getAttribute( String key )
   {
      return _element.getAttributeValue( key );
   }

   public Color getBackgroundColor()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.BACKGROUND );
      if( value == null )
         return null;

      // Skylight: #rgb, #rrggbb and rgb(r, g, b), read from the inline style
      Color skylightColor = parseColor( getInlineStyle( Css.Property.BACKGROUND ) );
      if( skylightColor != null )
         return skylightColor;

      if( value.getType() == CssValue.IDENT )
      {
         // todo: check why getType is IDENT and not STRING

         String stringValue = ( ( CssString ) value ).getStringValue();

         Css.TypeInfo typeInfo = Css.getTypeInfo( stringValue );

         if( typeInfo.getMajor() == Css.Type.COLOR )
         {
            return Css.getColor( typeInfo.getMinor() );
         }
         else
         {
            Xul.SyntaxError.UNKNOWN_CSS_PROPERTY_VALUE( stringValue, Css.Property.BACKGROUND );
         }
      }
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.BACKGROUND );
      }

      return null;
   }

   public boolean getBooleanAttribute( String key, boolean defaultValue )
   {
      String value = getAttribute( key );
      if( value == null )
         return defaultValue;
      else
      {
         value = value.toLowerCase().trim();
         if( value.equals( "true" ) || value.equals( "yes" ) || value.equals( "on" ) )
            return true;
         else
            return false;
      }
   }


   public Border getBorder()
   {
      // Skylight: padding goes inside the border, margin outside it
      Border border = getCssBorder();
      Insets padding = parseInsets( getInlineStyle( "padding" ) );
      Insets margin = parseInsets( getInlineStyle( "margin" ) );
      if( padding != null )
         border = border == null ? new EmptyBorder( padding ) : new CompoundBorder( border, new EmptyBorder( padding ) );
      if( margin != null )
         border = border == null ? new EmptyBorder( margin ) : new CompoundBorder( new EmptyBorder( margin ), border );
      return border;
   }

   /**
    *  Skylight: the value of one property in this element's style attribute
    *  (style="color: #336699; padding: 4 8"), or null.
    */
   public String getInlineStyle( String property )
   {
      String style = getAttribute( Xul.Attribute.STYLE );
      if( style == null )
         return null;
      String found = null;
      StringTokenizer declarations = new StringTokenizer( style, ";" );
      while( declarations.hasMoreTokens() )
      {
         String declaration = declarations.nextToken();
         int colon = declaration.indexOf( ':' );
         if( colon > 0 && declaration.substring( 0, colon ).trim().equalsIgnoreCase( property ) )
            found = declaration.substring( colon + 1 ).trim();
      }
      return found;
   }

   /** Skylight: #rgb, #rrggbb or rgb(r, g, b); null for anything else. */
   static Color parseColor( String text )
   {
      if( text == null )
         return null;
      text = text.trim();
      try
      {
         if( text.matches( "#[0-9a-fA-F]{3}" ) )
            return new Color( Integer.parseInt( text.substring( 1, 2 ), 16 ) * 17,
                  Integer.parseInt( text.substring( 2, 3 ), 16 ) * 17,
                  Integer.parseInt( text.substring( 3, 4 ), 16 ) * 17 );
         if( text.matches( "#[0-9a-fA-F]{6}" ) )
            return new Color( Integer.parseInt( text.substring( 1 ), 16 ) );
         if( text.toLowerCase().startsWith( "rgb(" ) && text.endsWith( ")" ) )
         {
            String[] parts = text.substring( 4, text.length() - 1 ).split( "," );
            if( parts.length == 3 )
               return new Color( Integer.parseInt( parts[0].trim() ), Integer.parseInt( parts[1].trim() ),
                     Integer.parseInt( parts[2].trim() ) );
         }
      }
      catch( IllegalArgumentException ex )
      {
         // not a colour we understand: Luxor's own checks report it
      }
      return null;
   }

   /** Skylight: CSS-style "4", "4 8", "4 8 2" or "4 8 2 6" (px optional); null if missing or wrong. */
   static Insets parseInsets( String text )
   {
      if( text == null )
         return null;
      String[] parts = text.trim().split( "\\s+" );
      int[] n = new int[parts.length];
      try
      {
         for( int i = 0; i < parts.length; i++ )
            n[i] = Integer.parseInt( parts[i].replaceAll( "px$", "" ) );
      }
      catch( NumberFormatException ex )
      {
         Xul.syntax( "unsupported value '" + text + "' for css property padding/margin" );
         return null;
      }
      switch ( n.length )
      {
         case 1:
            return new Insets( n[0], n[0], n[0], n[0] );
         case 2:
            return new Insets( n[0], n[1], n[0], n[1] );
         case 3:
            return new Insets( n[0], n[1], n[2], n[1] );
         case 4:
            return new Insets( n[0], n[1], n[2], n[3] );
         default:
            Xul.syntax( "unsupported value '" + text + "' for css property padding/margin" );
            return null;
      }
   }

   private Border getCssBorder()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.BORDER );
      if( value == null )
         return null;

      CssBorderDef borderDef = new CssBorderDef();

      do
      {
         if( value.getType() == CssValue.INTEGER )
         {
            // assume border-width property
            borderDef.setWidth( ( ( CssInteger ) value ).getIntegerValue() );
         }
         else if( value.getType() == CssValue.PIXEL )
         {
            // assume border-width property
            borderDef.setWidth( ( int ) ( ( CssFloat ) value ).getFloatValue() );
         }
         else if( value.getType() == CssValue.IDENT )
         {
            // todo: check why getType is IDENT and not STRING

            String stringValue = ( ( CssString ) value ).getStringValue();

            Css.TypeInfo typeInfo = Css.getTypeInfo( stringValue );

            if( typeInfo.getMajor() == Css.Type.BORDER_STYLE )
            {
               borderDef.setStyle( typeInfo.getMinor() );
            }
            else if( typeInfo.getMajor() == Css.Type.COLOR )
            {
               borderDef.setColor( Css.getColor( typeInfo.getMinor() ) );
            }
            else
            {
               Xul.SyntaxError.UNKNOWN_CSS_PROPERTY_VALUE( stringValue, Css.Property.BORDER );
            }
         }
         else
         {
            Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.BORDER );
         }

         value = value.getNext();
      }while ( value != null );

      Border border = null;

      if( borderDef.getStyle() == Css.BorderStyle.Id.NONE )
      {
         int width = 0;
         if( borderDef.useWidth() )
            width = borderDef.getWidth();

         border = new EmptyBorder( width, width, width, width );
         return border;
      }
      else if( borderDef.getStyle() == Css.BorderStyle.Id.SOLID )
      {
         Color color = Color.BLACK;
         int thickness = 1;
         if( borderDef.useColor() )
            color = borderDef.getColor();
         if( borderDef.useWidth() )
            thickness = borderDef.getWidth();

         border = new LineBorder( color, thickness );
         return border;
      }
      else if( borderDef.getStyle() == Css.BorderStyle.Id.DOUBLE )
      {
         Color color = Color.BLACK;
         int thickness = 1;
         if( borderDef.useColor() )
            color = borderDef.getColor();
         if( borderDef.useWidth() )
            thickness = borderDef.getWidth();

         border = new CompoundBorder( new LineBorder( color, thickness ),
               new CompoundBorder( new EmptyBorder( 1, 1, 1, 1 ), new LineBorder( color, thickness ) ) );
         return border;
      }
      else if( borderDef.getStyle() == Css.BorderStyle.Id.GROOVE )
      {
         border = new EtchedBorder( EtchedBorder.LOWERED );
         return border;
      }
      else if( borderDef.getStyle() == Css.BorderStyle.Id.RIDGE )
      {
         border = new EtchedBorder( EtchedBorder.RAISED );
         return border;
      }
      else if( borderDef.getStyle() == Css.BorderStyle.Id.INSET )
      {
         border = new BevelBorder( BevelBorder.LOWERED );
         return border;
      }
      else if( borderDef.getStyle() == Css.BorderStyle.Id.OUTSET )
      {
         border = new BevelBorder( BevelBorder.RAISED );
         return border;
      }
      else
      {
         Xul.syntax( "unsupported border-style for css property" );
         return null;
      }
   }

   public boolean getChecked()
   {
      return getBooleanAttribute( Xul.Attribute.CHECKED, false );
   }

   public int getColSpan()
   {
      return getIntAttribute( Xul.Attribute.COLSPAN, 1 );
   }

   public boolean getCollapsible()
   {
      return getBooleanAttribute( Xul.Attribute.COLLAPSIBLE, false );
   }

   public Color getColor()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.COLOR );
      if( value == null )
         return null;

      // Skylight: #rgb, #rrggbb and rgb(r, g, b), read from the inline style
      Color skylightColor = parseColor( getInlineStyle( Css.Property.COLOR ) );
      if( skylightColor != null )
         return skylightColor;

      if( value.getType() == CssValue.IDENT )
      {
         // todo: check why getType is IDENT and not STRING

         String stringValue = ( ( CssString ) value ).getStringValue();

         Css.TypeInfo typeInfo = Css.getTypeInfo( stringValue );

         if( typeInfo.getMajor() == Css.Type.COLOR )
         {
            return Css.getColor( typeInfo.getMinor() );
         }
         else
         {
            Xul.SyntaxError.UNKNOWN_CSS_PROPERTY_VALUE( stringValue, Css.Property.COLOR );
         }
      }
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.COLOR );
      }

      return null;
   }

   public int getCols( int defaultValue )
   {
      return getIntAttribute( Xul.Attribute.COLS, defaultValue );
   }

   public JPopupMenu getContextPopup( PopupResolver resolver )
   {
      String popupKey = getAttribute( Xul.Attribute.CONTEXT );
      if( popupKey == null )
         return null;

      return resolver.lookupPopup( popupKey );
   }

   public Element getElement()
   {
      return _element;
   }

   public String getElementName()
   {
      return _element.getName();
   }

   public int getFlex()
   {
      return getIntAttribute( Xul.Attribute.FLEX, 0 );
   }


   public Font getFont()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.FONT );
      if( value == null )
         return null;

      CssFontDef fontDef = new CssFontDef();
      String javaFamily = null;   // Skylight

      do
      {
         if( value.getType() == CssValue.INTEGER )
         {
            // assume font-size property
            fontDef.setSize( ( ( CssInteger ) value ).getIntegerValue() );
         }
         else if( value.getType() == CssValue.PIXEL )
         {
            // assume font-size property
            fontDef.setSize( ( int ) ( ( CssFloat ) value ).getFloatValue() );
         }
         else if( value.getType() == CssValue.IDENT )
         {
            // todo: check why getType is IDENT and not STRING

            String stringValue = ( ( CssString ) value ).getStringValue();

            Css.TypeInfo typeInfo = Css.getTypeInfo( stringValue );

            if( typeInfo.getMajor() == Css.Type.FONT_FAMILY )
            {
               fontDef.setFamily( typeInfo.getMinor() );
            }
            else if( typeInfo.getMajor() == Css.Type.FONT_STYLE )
            {
               fontDef.setStyle( typeInfo.getMinor() );
            }
            else if( typeInfo.getMajor() == Css.Type.FONT_WEIGHT )
            {
               fontDef.setWeight( typeInfo.getMinor() );
            }
            else
            {
               // Skylight: any other name is a Java font family (monospaced, Dialog, DejaVu Sans, ...)
               javaFamily = stringValue;
            }
         }
         else if( value.getType() == CssValue.STRING )
         {
            // Skylight: a quoted family name, "DejaVu Sans"
            javaFamily = ( ( CssString ) value ).getStringValue();
         }
         else
         {
            Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.FONT );
         }

         value = value.getNext();
      }while ( value != null );

      Font font = null;

      int size = 11;
      if( fontDef.useSize() )
         size = fontDef.getSize();

      int fontStyle = Font.PLAIN;
      if( fontDef.useStyle() )
         if( fontDef.getStyle() == Css.FontStyle.Id.ITALIC )
            fontStyle |= Font.ITALIC;

      if( fontDef.useWeight() )
         if( fontDef.getWeight() == Css.FontWeight.Id.BOLD )
            fontStyle |= Font.BOLD;

      if( javaFamily != null )
      {
         return new Font( javaFamily, fontStyle, size );
      }
      else if( fontDef.getFamily() == Css.FontFamily.Id.SERIF )
      {
         font = new Font( "serif", fontStyle, size );
         return font;
      }
      else if( fontDef.getFamily() == Css.FontFamily.Id.SANS_SERIF )
      {
         font = new Font( "sansserif", fontStyle, size );
         return font;
      }
      else if( fontDef.getFamily() == Css.FontFamily.Id.MONOSPACE )
      {
         font = new Font( "monospaced", fontStyle, size );
         return font;
      }
      else
      {
         // Skylight: no family given (font: 13 bold), so keep Swing's usual one
         return new Font( "Dialog", fontStyle, size );
      }
   }

   public ButtonGroup getGroup( GroupResolver resolver )
   {
      String groupKey = getAttribute( Xul.Attribute.GROUP );
      if( groupKey == null )
         return null;

      // create group if it doesn't exist already
      ButtonGroup group = resolver.lookupGroup( groupKey );
      return group;
   }

   public String getHeading( String defaultValue )
   {
      return getAttribute( Xul.Attribute.HEADING, defaultValue );
   }

   public int getHeight( int defaultHeight )
   {
      int height = getHeight();
      if( height == -1 )
         return defaultHeight;

      return height;
   }

   public int getHeight()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.HEIGHT );
      if( value == null )
         return -1;

      if( value.getType() == CssValue.INTEGER )
         return ( ( CssInteger ) value ).getIntegerValue();
      else if( value.getType() == CssValue.PIXEL )
         return ( int ) ( ( CssFloat ) value ).getFloatValue();
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.HEIGHT );
         return -1;
      }
   }

   public HistoryModel getHistory( HistoryResolver resolver )
   {
      String historyKey = getAttribute( Xul.Attribute.HISTORY );
      if( historyKey == null )
         return null;

      HistoryModel history = resolver.lookupHistory( historyKey );
      if( history == null )
         Xul.Warning.REFERENCE_NOT_FOUND( "history", historyKey );

      return history;
   }

   public ImageIcon getIcon( IconResolver resolver )
   {
      String iconKey = getAttribute( Xul.Attribute.ICON );
      if( iconKey == null )
         return null;

      ImageIcon icon = resolver.lookupIcon( iconKey );
      if( icon == null )
         Xul.Warning.REFERENCE_NOT_FOUND( "icon", iconKey );

      return icon;
   }

   public String getId()
   {
      return _id;
   }

   public int getIntAttribute( String key, int defaultValue )
   {
      String value = getAttribute( key );
      if( value == null )
         return defaultValue;
      else
      {
         try
         {
            return Integer.parseInt( value );
         }
         catch( NumberFormatException nex )
         {
            Xul.warning( "invalid number format: " + nex.toString() );
            return defaultValue;
         }
      }
   }

   public String getKey()
   {
      return getAttribute( Xul.Attribute.KEY );
   }

   public String getLabel()
   {
      return getAttribute( Xul.Attribute.LABEL );
   }

   public int getLeft( int defaultValue )
   {
      return getIntAttribute( Xul.Attribute.LEFT, defaultValue );
   }

   public String[] getList( ListResolver resolver )
   {
      String listKey = getAttribute( Xul.Attribute.LIST );
      if( listKey == null )
         return null;

      String list[] = resolver.lookupList( listKey );
      if( list == null )
         Xul.Warning.REFERENCE_NOT_FOUND( "list", listKey );

      return list;
   }

   public DoubleStringMap getMap( MapResolver resolver )
   {
      String mapKey = getAttribute( Xul.Attribute.MAP );
      if( mapKey == null )
         return null;

      DoubleStringMap map = resolver.lookupMap( mapKey );
      if( map == null )
         Xul.Warning.REFERENCE_NOT_FOUND( "map", mapKey );

      return map;
   }

   public Object getMask( MaskResolver resolver )
   {
      String maskKey = getAttribute( Xul.Attribute.MASK );
      if( maskKey == null )
         return null;

      return resolver.lookupMask( maskKey );
   }

   public int getMaxHeight()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.MAX_HEIGHT );
      if( value == null )
         return -1;

      if( value.getType() == CssValue.INTEGER )
         return ( ( CssInteger ) value ).getIntegerValue();
      else if( value.getType() == CssValue.PIXEL )
         return ( int ) ( ( CssFloat ) value ).getFloatValue();
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.MAX_HEIGHT );
         return -1;
      }
   }

   public int getMaxWidth()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.MAX_WIDTH );
      if( value == null )
         return -1;

      if( value.getType() == CssValue.INTEGER )
         return ( ( CssInteger ) value ).getIntegerValue();
      else if( value.getType() == CssValue.PIXEL )
         return ( int ) ( ( CssFloat ) value ).getFloatValue();
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.MAX_WIDTH );
         return -1;
      }
   }

   public int getMinHeight()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.MIN_HEIGHT );
      if( value == null )
         return -1;

      if( value.getType() == CssValue.INTEGER )
         return ( ( CssInteger ) value ).getIntegerValue();
      else if( value.getType() == CssValue.PIXEL )
         return ( int ) ( ( CssFloat ) value ).getFloatValue();
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.MIN_HEIGHT );
         return -1;
      }
   }

   public int getMinWidth()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.MIN_WIDTH );
      if( value == null )
         return -1;

      if( value.getType() == CssValue.INTEGER )
         return ( ( CssInteger ) value ).getIntegerValue();
      else if( value.getType() == CssValue.PIXEL )
         return ( int ) ( ( CssFloat ) value ).getFloatValue();
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.MIN_WIDTH );
         return -1;
      }
   }

   public int getOrient( int defaultValue )
   {
      String orient = getAttribute( Xul.Attribute.ORIENT );
      if( orient == null )
         return defaultValue;

      orient = orient.toLowerCase().trim();
      if( orient.equals( "vertical" )
             || orient.equals( "vert" ) )
         return SwingConstants.VERTICAL;
      else
         return SwingConstants.HORIZONTAL;
   }

   public int getOrient()
   {
      return getOrient( SwingConstants.HORIZONTAL );
   }

   public int getRowSpan()
   {
      return getIntAttribute( Xul.Attribute.ROWSPAN, 1 );
   }

   public int getRows( int defaultValue )
   {
      return getIntAttribute( Xul.Attribute.ROWS, defaultValue );
   }

   public XPath getSelect( String defaultValue )
   {
      // fix: either throw exception or
      //  return prebuild empty xpath

      String expr = getAttribute( Xul.Attribute.SELECT, defaultValue );

      XPath xpath = createXPath( expr );
      if( xpath != null )
         return xpath;

      // use default value as fallback
      if( !expr.equalsIgnoreCase( defaultValue ) )
      {
         Xul.warning( "using default XPath expr '" + defaultValue + "' as a fallback" );
         return createXPath( defaultValue );
      }
      else
         return null;
   }

   public StyleSheet getStyle()
   {
      if( _style == null )
      {
         String id = getId();
         String clazz = getStyleClass();
         String element = getElementName();
         boolean hasId = id.equals( "anonymous" );
         boolean hasClass = id.equals( "anonymous" );

         StyleRegister styleRegister = StyleRegister.getStyleRegister();
         if( hasId && hasClass )
            _style = styleRegister.getStyleSheet( id, clazz, element );
         else if( hasId )
            _style = styleRegister.getStyleSheet( id, element );
         else
            _style = styleRegister.getStyleSheet( element );

         // check for inline style declaration
         String styleDeclaration = _element.getAttributeValue( Xul.Attribute.STYLE );
         if( styleDeclaration != null )
         {
            Map inlineProps = XulCssStyleDeclarationParser.parse( styleDeclaration );
            _style = new StyleSheet( inlineProps, _style );
         }
      }
      return _style;
   }

   /**
    *  note, that I can't use simple getClass as this method is already used in
    *  java.lang.Object
    */
   public String getStyleClass()
   {
      return _styleClass;
   }

   public String getTooltip()
   {
      String tooltip = getAttribute( Xul.Attribute.TOOLTIPTEXT );
      // try non-compliant tooltip as an fallback/alternative
      if( tooltip == null )
         tooltip = getAttribute( Xul.Attribute.TOOLTIP );
      return tooltip;
   }

   public int getTop( int defaultValue )
   {
      return getIntAttribute( Xul.Attribute.TOP, defaultValue );
   }

   public int getType()
   {
      String value = getAttribute( Xul.Attribute.TYPE );

      if( value == null )
         return Xul.Type.DEFAULT;
      else
      {
         value = value.toLowerCase().trim();

         if( value.equals( Xul.Value.RADIO ) )
            return Xul.Type.RADIO;
         else if( value.equals( Xul.Value.CHECKBOX ) )
            return Xul.Type.CHECKBOX;
         else
         {
            Xul.warning( "ignoring unknown type attribute '" + value + "'" );
            // todo: add in element/tag xyz
            return Xul.Type.DEFAULT;
         }
      }
   }

   public String getValue()
   {
      return getAttribute( Xul.Attribute.VALUE );
   }

   public int getVerticalAlign( int defaultValue )
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.VERTICAL_ALIGN );
      if( value == null )
         return defaultValue;

      if( value.getType() == CssValue.IDENT )
      {
         // todo: check why getType is IDENT and not STRING

         String stringValue = ( ( CssString ) value ).getStringValue();

         Css.TypeInfo typeInfo = Css.getTypeInfo( stringValue );

         if( typeInfo.getMajor() == Css.Type.VERTICAL_ALIGN )
         {
            switch ( typeInfo.getMinor() )
            {
               case Css.VerticalAlign.Id.TOP:
                  return SwingConstants.TOP;
               case Css.VerticalAlign.Id.MIDDLE:
                  return SwingConstants.CENTER;
               case Css.VerticalAlign.Id.BOTTOM:
                  return SwingConstants.BOTTOM;
               // fallthrough for default case
            }
         }

         Xul.SyntaxError.UNKNOWN_CSS_PROPERTY_VALUE( stringValue, Css.Property.VERTICAL_ALIGN );
      }
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.VERTICAL_ALIGN );
      }

      return defaultValue;
   }

   public int getWidth( int defaultWidth )
   {
      int width = getWidth();
      if( width == -1 )
         return defaultWidth;

      return width;
   }

   public int getWidth()
   {
      StyleSheet style = getStyle();
      CssValue value = style.lookup( Css.Property.WIDTH );
      if( value == null )
         return -1;

      if( value.getType() == CssValue.INTEGER )
         return ( ( CssInteger ) value ).getIntegerValue();
      else if( value.getType() == CssValue.PIXEL )
         return ( int ) ( ( CssFloat ) value ).getFloatValue();
      else
      {
         Xul.SyntaxError.UNSUPPORTED_CSS_PROPERTY_VALUE_TYPE( value.getType(), Css.Property.WIDTH );
         return -1;
      }
   }

   // convenience method; hides XulResourceHelper
   public ImageIcon loadIcon( XulResourceLoader loader, String name )
   {
      return XulResourceHelper.loadIcon( loader, name );
   }

   public String loadText( XulResourceLoader loader, String name ) throws IOException
   {
      return XulResourceHelper.loadText( loader, name );
   }

   public Document loadXmlDocument( XulResourceLoader loader, String name ) throws JDOMException
   {
      return XulResourceHelper.loadXmlDocument( loader, name );
   }

   private XPath createXPath( String expr )
   {
      try
      {
         return new XPath( expr );
      }
      catch( XPathSyntaxException xex )
      {
         Xul.Error.XPATH_PARSING_EXCEPTION( expr, xex );
      }
      catch( JaxenException jex )
      {
         Xul.Error.XPATH_CREATION_EXCEPTION( expr, jex );
      }
      return null;
   }
}

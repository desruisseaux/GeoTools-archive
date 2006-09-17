/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.filter.function;


//this code is autogenerated - you shouldnt be modifying it!

import com.vividsolutions.jts.geom.*; //generic JTS support

import org.geotools.filter.function.StaticGeometry; //make sure you include this or you will not be able to call the functions!!


import org.geotools.feature.Feature;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.LiteralExpression;


public class FilterFunction_in8 extends FunctionExpressionImpl implements FunctionExpression 
{


private Expression[] args; // list of args that this functions needs


public FilterFunction_in8()
{
}


public String getName()
{
      return "in8";
}

public int getArgCount()
{
      return 9;
}

public void setArgs(Expression[] args)
{
      this.args = args;
}

public Expression[] getArgs()
{
       return args;
}

public String toString()
{
       String result =  "in8(";
       for (int t=0;t<args.length;t++)
       {
                result += args[t] +",";
       }
       result += ")";
       return result;
}

public Object evaluate(Feature feature)
{
      Object  arg0;
      Object  arg1;
      Object  arg2;
      Object  arg3;
      Object  arg4;
      Object  arg5;
      Object  arg6;
      Object  arg7;
      Object  arg8;


      try{  //attempt to get value and perform conversion
                arg0 = (Object) args[0].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function in8 argument #0 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg1 = (Object) args[1].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function in8 argument #1 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg2 = (Object) args[2].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function in8 argument #2 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg3 = (Object) args[3].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function in8 argument #3 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg4 = (Object) args[4].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function in8 argument #4 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg5 = (Object) args[5].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function in8 argument #5 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg6 = (Object) args[6].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function in8 argument #6 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg7 = (Object) args[7].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function in8 argument #7 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg8 = (Object) args[8].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function in8 argument #8 - expected type Object");
      }

      return new Boolean(StaticGeometry.in8(arg0,arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8 ));
}
}


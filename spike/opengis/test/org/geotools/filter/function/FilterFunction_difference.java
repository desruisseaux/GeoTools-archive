package org.geotools.filter.function;


//this code is autogenerated - you shouldnt be modifying it!

import com.vividsolutions.jts.geom.*; //generic JTS support
import org.geotools.filter.function.StaticGeometry; //make sure you include this or you will not be able to call the functions!!


import org.opengis.feature.Attribute;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.filter.LiteralExpression;


public class FilterFunction_difference extends FunctionExpressionImpl implements FunctionExpression 
{


private Expression[] args; // list of args that this functions needs


public FilterFunction_difference()
{
}


public String getName()
{
      return "difference";
}

public int getArgCount()
{
      return 2;
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
       String result =  "difference(";
       for (int t=0;t<args.length;t++)
       {
                result += args[t] +",";
       }
       result += ")";
       return result;
}

public Object getValue(Attribute att)
{
      Geometry  arg0;
      Geometry  arg1;


      try{  //attempt to get value and perform conversion
                arg0 = (Geometry) args[0].getValue(att);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function difference argument #0 - expected type Geometry");
      }

      try{  //attempt to get value and perform conversion
                arg1 = (Geometry) args[1].getValue(att);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function difference argument #1 - expected type Geometry");
      }

      return (StaticGeometry.difference(arg0,arg1 ));
}
}


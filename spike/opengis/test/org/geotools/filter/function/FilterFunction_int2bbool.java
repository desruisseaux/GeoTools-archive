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


public class FilterFunction_int2bbool extends FunctionExpressionImpl implements FunctionExpression 
{


private Expression[] args; // list of args that this functions needs


public FilterFunction_int2bbool()
{
}


public String getName()
{
      return "int2bbool";
}

public int getArgCount()
{
      return 1;
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
       String result =  "int2bbool(";
       for (int t=0;t<args.length;t++)
       {
                result += args[t] +",";
       }
       result += ")";
       return result;
}

public Object getValue(Attribute att)
{
      int  arg0;


      try{  //attempt to get value and perform conversion
                arg0 = ((Number) args[0].getValue(att)).intValue();
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function int2bbool argument #0 - expected type int");
      }

      return new Boolean(StaticGeometry.int2bbool(arg0 ));
}
}


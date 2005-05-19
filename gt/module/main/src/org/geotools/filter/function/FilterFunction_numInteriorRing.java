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


public class FilterFunction_numInteriorRing extends FunctionExpressionImpl implements FunctionExpression 
{


private Expression[] args; // list of args that this functions needs


public FilterFunction_numInteriorRing()
{
}


public String getName()
{
      return "numInteriorRing";
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
       String result =  "numInteriorRing(";
       for (int t=0;t<args.length;t++)
       {
                result += args[t] +",";
       }
       result += ")";
       return result;
}

public Object getValue(Feature feature)
{
      Geometry  arg0;


      try{  //attempt to get value and perform conversion
                arg0 = (Geometry) args[0].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function numInteriorRing argument #0 - expected type Geometry");
      }

      return new Integer(StaticGeometry.numInteriorRing(arg0 ));
}
}


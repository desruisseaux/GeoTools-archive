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


public class FilterFunction_between extends FunctionExpressionImpl implements FunctionExpression 
{


private Expression[] args; // list of args that this functions needs


public FilterFunction_between()
{
}


public String getName()
{
      return "between";
}

public int getArgCount()
{
      return 3;
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
       String result =  "between(";
       for (int t=0;t<args.length;t++)
       {
                result += args[t] +",";
       }
       result += ")";
       return result;
}

public Object getValue(Feature feature)
{
      Object  arg0;
      Object  arg1;
      Object  arg2;


      try{  //attempt to get value and perform conversion
                arg0 = (Object) args[0].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function between argument #0 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg1 = (Object) args[1].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function between argument #1 - expected type Object");
      }

      try{  //attempt to get value and perform conversion
                arg2 = (Object) args[2].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function between argument #2 - expected type Object");
      }

      return new Boolean(StaticGeometry.between(arg0,arg1,arg2 ));
}
}


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


public class FilterFunction_relatePattern extends FunctionExpressionImpl implements FunctionExpression 
{


private Expression[] args; // list of args that this functions needs


public FilterFunction_relatePattern()
{
}


public String getName()
{
      return "relatePattern";
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
       String result =  "relatePattern(";
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
      Geometry  arg1;
      String  arg2;


      try{  //attempt to get value and perform conversion
                arg0 = (Geometry) args[0].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function relatePattern argument #0 - expected type Geometry");
      }

      try{  //attempt to get value and perform conversion
                arg1 = (Geometry) args[1].getValue(feature);
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function relatePattern argument #1 - expected type Geometry");
      }

      try{  //attempt to get value and perform conversion
                arg2 = (args[2].getValue(feature)).toString(); // extra protection for strings
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function relatePattern argument #2 - expected type String");
      }

      return new Boolean(StaticGeometry.relatePattern(arg0,arg1,arg2 ));
}
}


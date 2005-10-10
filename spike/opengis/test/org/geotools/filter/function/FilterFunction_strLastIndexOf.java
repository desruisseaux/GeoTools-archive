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


public class FilterFunction_strLastIndexOf extends FunctionExpressionImpl implements FunctionExpression 
{


private Expression[] args; // list of args that this functions needs


public FilterFunction_strLastIndexOf()
{
}


public String getName()
{
      return "strLastIndexOf";
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
       String result =  "strLastIndexOf(";
       for (int t=0;t<args.length;t++)
       {
                result += args[t] +",";
       }
       result += ")";
       return result;
}

public Object getValue(Attribute att)
{
      String  arg0;
      String  arg1;


      try{  //attempt to get value and perform conversion
                arg0 = (args[0].getValue(att)).toString(); // extra protection for strings
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function strLastIndexOf argument #0 - expected type String");
      }

      try{  //attempt to get value and perform conversion
                arg1 = (args[1].getValue(att)).toString(); // extra protection for strings
      }
      catch (Exception e) // probably a type error
      {
            throw new IllegalArgumentException("Filter Function problem for function strLastIndexOf argument #1 - expected type String");
      }

      return new Integer(StaticGeometry.strLastIndexOf(arg0,arg1 ));
}
}


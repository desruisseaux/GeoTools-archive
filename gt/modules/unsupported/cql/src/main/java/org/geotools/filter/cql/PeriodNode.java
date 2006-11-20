package org.geotools.filter.cql;

import java.security.InvalidParameterException;
import java.util.Date;

import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Literal;

/**
 * Period is consturcted in the parseing process. this has convenient method to
 * deliver begin and end date of period. a period can be created from
 * date-time/date-time or date-time/duration or duration/date-time
 * 
 * @author Mauricio Pazos - Axios Engineering
 * @author Gabriel Roldan - Axios Engineering
 * @version $Id$
 * @source $URL$
 * 
 */
class PeriodNode {

    private Literal begin = null;

    private Literal end = null;

    /**
     * @see create
     * 
     * @param begin
     * @param end
     */
    private PeriodNode(final Literal begin, final Literal end) {

        if (!(begin.getValue() instanceof Date))
            throw new InvalidParameterException(
                    "begin parameter must be Literal with Date");
        if (!(begin.getValue() instanceof Date))
            throw new InvalidParameterException(
                    "end paremeter must be Literal with Date");

        this.begin = begin;
        this.end = end;
    }

    public static PeriodNode createPeriodDateAndDate(final Literal beginDate,
            final Literal endDate) {

        PeriodNode period = new PeriodNode(beginDate, endDate);

        return period;
    }

    public static PeriodNode createPeriodDateAndDuration(final Literal date,
            final Literal duration, FilterFactory filterFactory) {

        // compute last date from duration
        // Y M D and H M S
        Date firstDate = (Date) date.getValue();
        String strDuration = (String) duration.getValue();

        Date lastDate = Util.addDurationToDate(firstDate, strDuration);

        Literal literalLastDate = filterFactory.literal(lastDate);

        PeriodNode period = new PeriodNode(date, literalLastDate);

        return period;
    }

    public static PeriodNode createPeriodDurationAndDate(
            final Literal duration, final Literal date,
            FilterFactory filterFactory) {

        // compute first date from duration Y M D and H M S
        Date lastDate = (Date) date.getValue();
        String strDuration = (String) duration.getValue();

        Date firstDate = Util.subtractDurationToDate(lastDate, strDuration);

        Literal literalFirstDate = filterFactory.literal(firstDate);

        PeriodNode period = new PeriodNode(literalFirstDate, date);

        return period;
    }

    /**
     * @return Literal with begining date of period
     */
    public Literal getBeginning() {
        return this.begin;
    }

    /**
     * @return with ending date of period
     */
    public Literal getEnding() {
        return this.end;
    }

}

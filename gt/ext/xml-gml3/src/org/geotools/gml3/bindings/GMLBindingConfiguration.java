package org.geotools.gml3.bindings;


import org.geotools.gml2.bindings.GMLCoordTypeBinding;
import org.geotools.gml2.bindings.GMLCoordinatesTypeBinding;
import org.geotools.xml.BindingConfiguration;
import org.picocontainer.MutablePicoContainer;

/**
 * Binding configuration for the http://www.opengis.net/gml schema.
 *
 * @generated
 */
public final class GMLBindingConfiguration
	implements BindingConfiguration {


	/**
	 * @generated modifiable
	 */
	public void configure(MutablePicoContainer container) {
	
		//Types
		container.registerComponentImplementation(GML.ABSTRACTCURVESEGMENTTYPE,AbstractCurveSegmentTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTCURVETYPE,AbstractCurveTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTGEOMETRICAGGREGATETYPE,AbstractGeometricAggregateTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTGEOMETRICPRIMITIVETYPE,AbstractGeometricPrimitiveTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTGEOMETRYTYPE,AbstractGeometryTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTGMLTYPE,AbstractGMLTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTGRIDDEDSURFACETYPE,AbstractGriddedSurfaceTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTMETADATATYPE,AbstractMetaDataTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTPARAMETRICCURVESURFACETYPE,AbstractParametricCurveSurfaceTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTRINGPROPERTYTYPE,AbstractRingPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTRINGTYPE,AbstractRingTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTSOLIDTYPE,AbstractSolidTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTSURFACEPATCHTYPE,AbstractSurfacePatchTypeBinding.class);
		container.registerComponentImplementation(GML.ABSTRACTSURFACETYPE,AbstractSurfaceTypeBinding.class);
		container.registerComponentImplementation(GML.AFFINEPLACEMENTTYPE,AffinePlacementTypeBinding.class);
		container.registerComponentImplementation(GML.ANGLECHOICETYPE,AngleChoiceTypeBinding.class);
		container.registerComponentImplementation(GML.ANGLETYPE,AngleTypeBinding.class);
		container.registerComponentImplementation(GML.ARCBYBULGETYPE,ArcByBulgeTypeBinding.class);
		container.registerComponentImplementation(GML.ARCBYCENTERPOINTTYPE,ArcByCenterPointTypeBinding.class);
		container.registerComponentImplementation(GML.ARCMINUTESTYPE,ArcMinutesTypeBinding.class);
		container.registerComponentImplementation(GML.ARCSECONDSTYPE,ArcSecondsTypeBinding.class);
		container.registerComponentImplementation(GML.ARCSTRINGBYBULGETYPE,ArcStringByBulgeTypeBinding.class);
		container.registerComponentImplementation(GML.ARCSTRINGTYPE,ArcStringTypeBinding.class);
		container.registerComponentImplementation(GML.ARCTYPE,ArcTypeBinding.class);
		container.registerComponentImplementation(GML.AREATYPE,AreaTypeBinding.class);
		container.registerComponentImplementation(GML.ARRAYASSOCIATIONTYPE,ArrayAssociationTypeBinding.class);
		container.registerComponentImplementation(GML.ARRAYTYPE,ArrayTypeBinding.class);
		container.registerComponentImplementation(GML.ASSOCIATIONTYPE,AssociationTypeBinding.class);
		container.registerComponentImplementation(GML.BAGTYPE,BagTypeBinding.class);
		container.registerComponentImplementation(GML.BASEUNITTYPE,BaseUnitTypeBinding.class);
		container.registerComponentImplementation(GML.BEZIERTYPE,BezierTypeBinding.class);
		container.registerComponentImplementation(GML.BOOLEANLIST,BooleanListBinding.class);
		container.registerComponentImplementation(GML.BOOLEANORNULL,BooleanOrNullBinding.class);
		container.registerComponentImplementation(GML.BOOLEANORNULLLIST,BooleanOrNullListBinding.class);
		container.registerComponentImplementation(GML.BSPLINETYPE,BSplineTypeBinding.class);
		container.registerComponentImplementation(GML.CIRCLEBYCENTERPOINTTYPE,CircleByCenterPointTypeBinding.class);
		container.registerComponentImplementation(GML.CIRCLETYPE,CircleTypeBinding.class);
		container.registerComponentImplementation(GML.CLOTHOIDTYPE,ClothoidTypeBinding.class);
		container.registerComponentImplementation(GML.CODELISTTYPE,CodeListTypeBinding.class);
		container.registerComponentImplementation(GML.CODEORNULLLISTTYPE,CodeOrNullListTypeBinding.class);
		container.registerComponentImplementation(GML.CODETYPE,CodeTypeBinding.class);
		container.registerComponentImplementation(GML.CONETYPE,ConeTypeBinding.class);
		container.registerComponentImplementation(GML.CONVENTIONALUNITTYPE,ConventionalUnitTypeBinding.class);
		container.registerComponentImplementation(GML.CONVERSIONTOPREFERREDUNITTYPE,ConversionToPreferredUnitTypeBinding.class);
		//container.registerComponentImplementation(GML.COORDINATESTYPE,CoordinatesTypeBinding.class);
		container.registerComponentImplementation(GML.COORDINATESTYPE,GMLCoordinatesTypeBinding.class);
		//container.registerComponentImplementation(GML.COORDTYPE,CoordTypeBinding.class);
		container.registerComponentImplementation(GML.COORDTYPE,GMLCoordTypeBinding.class);
		container.registerComponentImplementation(GML.CUBICSPLINETYPE,CubicSplineTypeBinding.class);
		container.registerComponentImplementation(GML.CURVEARRAYPROPERTYTYPE,CurveArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.CURVEINTERPOLATIONTYPE,CurveInterpolationTypeBinding.class);
		container.registerComponentImplementation(GML.CURVEPROPERTYTYPE,CurvePropertyTypeBinding.class);
		container.registerComponentImplementation(GML.CURVESEGMENTARRAYPROPERTYTYPE,CurveSegmentArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.CURVETYPE,CurveTypeBinding.class);
		container.registerComponentImplementation(GML.CYLINDERTYPE,CylinderTypeBinding.class);
		container.registerComponentImplementation(GML.DECIMALMINUTESTYPE,DecimalMinutesTypeBinding.class);
		container.registerComponentImplementation(GML.DEFINITIONPROXYTYPE,DefinitionProxyTypeBinding.class);
		container.registerComponentImplementation(GML.DEFINITIONTYPE,DefinitionTypeBinding.class);
		container.registerComponentImplementation(GML.DEGREESTYPE,DegreesTypeBinding.class);
		container.registerComponentImplementation(GML.DEGREEVALUETYPE,DegreeValueTypeBinding.class);
		container.registerComponentImplementation(GML.DERIVATIONUNITTERMTYPE,DerivationUnitTermTypeBinding.class);
		container.registerComponentImplementation(GML.DERIVEDUNITTYPE,DerivedUnitTypeBinding.class);
		container.registerComponentImplementation(GML.DICTIONARYENTRYTYPE,DictionaryEntryTypeBinding.class);
		container.registerComponentImplementation(GML.DICTIONARYTYPE,DictionaryTypeBinding.class);
		container.registerComponentImplementation(GML.DIRECTPOSITIONLISTTYPE,DirectPositionListTypeBinding.class);
		container.registerComponentImplementation(GML.DIRECTPOSITIONTYPE,DirectPositionTypeBinding.class);
		container.registerComponentImplementation(GML.DMSANGLETYPE,DMSAngleTypeBinding.class);
		container.registerComponentImplementation(GML.DOUBLELIST,DoubleListBinding.class);
		container.registerComponentImplementation(GML.DOUBLEORNULL,DoubleOrNullBinding.class);
		container.registerComponentImplementation(GML.DOUBLEORNULLLIST,DoubleOrNullListBinding.class);
		container.registerComponentImplementation(GML.ENVELOPETYPE,EnvelopeTypeBinding.class);
		container.registerComponentImplementation(GML.FORMULATYPE,FormulaTypeBinding.class);
		container.registerComponentImplementation(GML.GENERICMETADATATYPE,GenericMetaDataTypeBinding.class);
		container.registerComponentImplementation(GML.GEODESICSTRINGTYPE,GeodesicStringTypeBinding.class);
		container.registerComponentImplementation(GML.GEODESICTYPE,GeodesicTypeBinding.class);
		container.registerComponentImplementation(GML.GEOMETRICPRIMITIVEPROPERTYTYPE,GeometricPrimitivePropertyTypeBinding.class);
		container.registerComponentImplementation(GML.GEOMETRYARRAYPROPERTYTYPE,GeometryArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.GEOMETRYPROPERTYTYPE,GeometryPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.GRIDLENGTHTYPE,GridLengthTypeBinding.class);
		container.registerComponentImplementation(GML.INDIRECTENTRYTYPE,IndirectEntryTypeBinding.class);
		container.registerComponentImplementation(GML.INTEGERLIST,IntegerListBinding.class);
		container.registerComponentImplementation(GML.INTEGERORNULL,IntegerOrNullBinding.class);
		container.registerComponentImplementation(GML.INTEGERORNULLLIST,IntegerOrNullListBinding.class);
		container.registerComponentImplementation(GML.KNOTPROPERTYTYPE,KnotPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.KNOTTYPE,KnotTypeBinding.class);
		container.registerComponentImplementation(GML.KNOTTYPESTYPE,KnotTypesTypeBinding.class);
		container.registerComponentImplementation(GML.LENGTHTYPE,LengthTypeBinding.class);
		container.registerComponentImplementation(GML.LINEARRINGPROPERTYTYPE,LinearRingPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.LINEARRINGTYPE,LinearRingTypeBinding.class);
		container.registerComponentImplementation(GML.LINESTRINGPROPERTYTYPE,LineStringPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.LINESTRINGSEGMENTARRAYPROPERTYTYPE,LineStringSegmentArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.LINESTRINGSEGMENTTYPE,LineStringSegmentTypeBinding.class);
		container.registerComponentImplementation(GML.LINESTRINGTYPE,LineStringTypeBinding.class);
		container.registerComponentImplementation(GML.MEASURELISTTYPE,MeasureListTypeBinding.class);
		container.registerComponentImplementation(GML.MEASUREORNULLLISTTYPE,MeasureOrNullListTypeBinding.class);
		container.registerComponentImplementation(GML.MEASURETYPE,MeasureTypeBinding.class);
		container.registerComponentImplementation(GML.METADATAPROPERTYTYPE,MetaDataPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.MULTICURVEPROPERTYTYPE,MultiCurvePropertyTypeBinding.class);
		container.registerComponentImplementation(GML.MULTICURVETYPE,MultiCurveTypeBinding.class);
		container.registerComponentImplementation(GML.MULTIGEOMETRYPROPERTYTYPE,MultiGeometryPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.MULTIGEOMETRYTYPE,MultiGeometryTypeBinding.class);
		container.registerComponentImplementation(GML.MULTILINESTRINGPROPERTYTYPE,MultiLineStringPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.MULTILINESTRINGTYPE,MultiLineStringTypeBinding.class);
		container.registerComponentImplementation(GML.MULTIPOINTPROPERTYTYPE,MultiPointPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.MULTIPOINTTYPE,MultiPointTypeBinding.class);
		container.registerComponentImplementation(GML.MULTIPOLYGONPROPERTYTYPE,MultiPolygonPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.MULTIPOLYGONTYPE,MultiPolygonTypeBinding.class);
		container.registerComponentImplementation(GML.MULTISOLIDPROPERTYTYPE,MultiSolidPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.MULTISOLIDTYPE,MultiSolidTypeBinding.class);
		container.registerComponentImplementation(GML.MULTISURFACEPROPERTYTYPE,MultiSurfacePropertyTypeBinding.class);
		container.registerComponentImplementation(GML.MULTISURFACETYPE,MultiSurfaceTypeBinding.class);
		container.registerComponentImplementation(GML.NAMELIST,NameListBinding.class);
		container.registerComponentImplementation(GML.NAMEORNULL,NameOrNullBinding.class);
		container.registerComponentImplementation(GML.NAMEORNULLLIST,NameOrNullListBinding.class);
		container.registerComponentImplementation(GML.NCNAMELIST,NCNameListBinding.class);
		container.registerComponentImplementation(GML.NULLENUMERATION,NullEnumerationBinding.class);
		container.registerComponentImplementation(GML.NULLTYPE,NullTypeBinding.class);
		container.registerComponentImplementation(GML.OFFSETCURVETYPE,OffsetCurveTypeBinding.class);
		container.registerComponentImplementation(GML.ORIENTABLECURVETYPE,OrientableCurveTypeBinding.class);
		container.registerComponentImplementation(GML.ORIENTABLESURFACETYPE,OrientableSurfaceTypeBinding.class);
		container.registerComponentImplementation(GML.POINTARRAYPROPERTYTYPE,PointArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.POINTPROPERTYTYPE,PointPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.POINTTYPE,PointTypeBinding.class);
		container.registerComponentImplementation(GML.POLYGONPATCHARRAYPROPERTYTYPE,PolygonPatchArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.POLYGONPATCHTYPE,PolygonPatchTypeBinding.class);
		container.registerComponentImplementation(GML.POLYGONPROPERTYTYPE,PolygonPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.POLYGONTYPE,PolygonTypeBinding.class);
		container.registerComponentImplementation(GML.POLYHEDRALSURFACETYPE,PolyhedralSurfaceTypeBinding.class);
		container.registerComponentImplementation(GML.QNAMELIST,QNameListBinding.class);
		container.registerComponentImplementation(GML.RECTANGLETYPE,RectangleTypeBinding.class);
		container.registerComponentImplementation(GML.REFERENCETYPE,ReferenceTypeBinding.class);
		container.registerComponentImplementation(GML.RINGPROPERTYTYPE,RingPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.RINGTYPE,RingTypeBinding.class);
		container.registerComponentImplementation(GML.SCALETYPE,ScaleTypeBinding.class);
		container.registerComponentImplementation(GML.SIGNTYPE,SignTypeBinding.class);
		container.registerComponentImplementation(GML.SOLIDARRAYPROPERTYTYPE,SolidArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.SOLIDPROPERTYTYPE,SolidPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.SOLIDTYPE,SolidTypeBinding.class);
		container.registerComponentImplementation(GML.SPEEDTYPE,SpeedTypeBinding.class);
		container.registerComponentImplementation(GML.SPHERETYPE,SphereTypeBinding.class);
		container.registerComponentImplementation(GML.STRINGORNULL,StringOrNullBinding.class);
		container.registerComponentImplementation(GML.STRINGORREFTYPE,StringOrRefTypeBinding.class);
		container.registerComponentImplementation(GML.SURFACEARRAYPROPERTYTYPE,SurfaceArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.SURFACEINTERPOLATIONTYPE,SurfaceInterpolationTypeBinding.class);
		container.registerComponentImplementation(GML.SURFACEPATCHARRAYPROPERTYTYPE,SurfacePatchArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.SURFACEPROPERTYTYPE,SurfacePropertyTypeBinding.class);
		container.registerComponentImplementation(GML.SURFACETYPE,SurfaceTypeBinding.class);
		container.registerComponentImplementation(GML.TIMETYPE,TimeTypeBinding.class);
		container.registerComponentImplementation(GML.TINTYPE,TinTypeBinding.class);
		container.registerComponentImplementation(GML.TRIANGLEPATCHARRAYPROPERTYTYPE,TrianglePatchArrayPropertyTypeBinding.class);
		container.registerComponentImplementation(GML.TRIANGLETYPE,TriangleTypeBinding.class);
		container.registerComponentImplementation(GML.TRIANGULATEDSURFACETYPE,TriangulatedSurfaceTypeBinding.class);
		container.registerComponentImplementation(GML.UNITDEFINITIONTYPE,UnitDefinitionTypeBinding.class);
		container.registerComponentImplementation(GML.UNITOFMEASURETYPE,UnitOfMeasureTypeBinding.class);
		container.registerComponentImplementation(GML.VECTORTYPE,VectorTypeBinding.class);
		container.registerComponentImplementation(GML.VOLUMETYPE,VolumeTypeBinding.class);


	}

}
package org.geotools.renderer3d.terrainblock;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.TriMesh;
import com.jme.util.geom.BufferUtils;
import org.geotools.renderer3d.utils.MathUtils;
import org.geotools.renderer3d.utils.ParameterChecker;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * @author Hans Häggström
 */
public final class TerrainMesh
        extends TriMesh
{

    //======================================================================
    // Private Fields

    private static int theTerrainMeshCounter = 0;

    private final double myX1;
    private final double myY1;
    private final double myX2;
    private final double myY2;
    private final float myZ;
    private final FloatBuffer myVertexes;
    private final FloatBuffer myColors;
    private final FloatBuffer myTextureCoordinates;
    private final FloatBuffer myNormals;
    private final IntBuffer myIndices;
    private final int myNumberOfVertices;
    private final int myNumberOfCells;
    private final int myNumberOfIndices;
    private final int mySizeY_cells;
    private final int mySizeX_cells;
    private final int mySizeY_vertices;
    private final int mySizeX_vertices;

    //======================================================================
    // Public Methods

    //----------------------------------------------------------------------
    // Constructors

    /**
     * @param sizeXCells Number of grid cells along the X side.
     * @param sizeYCells Number of grid cells along the Y side.
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param z
     */
    public TerrainMesh( final int sizeXCells,
                        final int sizeYCells,
                        final double x1,
                        final double y1,
                        final double x2,
                        final double y2,
                        final float z )
    {
        // JME seems to need an unique identifier for each node.  NOTE: Not thread safe.
        super( "TerrainMesh_" + theTerrainMeshCounter++ );

        // Check parameters
        ParameterChecker.checkPositiveNonZeroInteger( sizeXCells, "sizeXCells" );
        ParameterChecker.checkPositiveNonZeroInteger( sizeYCells, "sizeYCells" );
        ParameterChecker.checkNormalNumber( x1, "x1" );
        ParameterChecker.checkNormalNumber( y1, "y1" );
        ParameterChecker.checkNormalNumber( x2, "x2" );
        ParameterChecker.checkNormalNumber( y2, "y2" );
        ParameterChecker.checkLargerThan( x2, "x2", x1, "x1" );
        ParameterChecker.checkLargerThan( y2, "y2", y1, "y1" );
        ParameterChecker.checkNormalNumber( z, "z" );

        // Assign fields from parameters
        mySizeX_cells = sizeXCells;
        mySizeY_cells = sizeYCells;
        mySizeX_vertices = mySizeX_cells + 1;
        mySizeY_vertices = mySizeY_cells + 1;
        myX1 = x1;
        myY1 = y1;
        myX2 = x2;
        myY2 = y2;
        myZ = z;

        // Calculate sizes
        myNumberOfVertices = ( mySizeX_cells + 1 ) * ( mySizeY_cells + 1 );
        myNumberOfCells = mySizeX_cells * mySizeY_cells;
        myNumberOfIndices = myNumberOfCells * 6;

        // Create databuffers
        myVertexes = BufferUtils.createVector3Buffer( myNumberOfVertices );
        myColors = BufferUtils.createColorBuffer( myNumberOfVertices );
        myTextureCoordinates = BufferUtils.createVector2Buffer( myNumberOfVertices );
        myNormals = BufferUtils.createVector3Buffer( myNumberOfVertices );
        myIndices = BufferUtils.createIntBuffer( myNumberOfIndices );

        // Put vertices in a grid formation in the correct place in the world
        layOutVetices();

        // Stich together the vertices into triangles
        initializeIndices();

        // Initialize the TriMesh
        setVertexBuffer( 0, myVertexes );
        setColorBuffer( 0, myColors );
        setTextureBuffer( 0, myTextureCoordinates );
        setNormalBuffer( 0, myNormals );
        setIndexBuffer( 0, myIndices );

        // Update bounding box
        setModelBound( new BoundingBox() );
        updateModelBound();
    }

    //======================================================================
    // Private Methods

    /* *
     * Applies the height data from the specified heightMap to the specified terrainMesh. The user should ensure that
     * the mesh and the heightmap are of the same size.
     *
     * @param terrainMesh  the mesh to update
     * @param heightMap    the height data
     * @param parentBuffer null or a larger height map, used to smoothen out the values near the edge.
     * @param isHollow     true of the current chunk is hollow.  If it is, the inside edges will be pulled down to avoid
     *                     one pixel holes in the mesh.
     * @param holeOffsetX
     * @param holeOffsetZ
     * /
    public void updateTerrainHeight( final TriMesh terrainMesh,
                                     final LocatedFloatBuffer2D heightMap,
                                     final LocatedFloatBuffer2D parentBuffer,
                                     final boolean isHollow,
                                     final int holeOffsetX,
                                     final int holeOffsetZ )
    {
        // Check parameters
        if ( terrainMesh == null )
        {
            throw new IllegalArgumentException( "The parameter terrainMesh should not be null." );
        }
        if ( heightMap == null )
        {
            throw new IllegalArgumentException( "The parameter heightMap should not be null." );
        }

        // Get variables
        final float minWorldX = heightMap.getRegion().getMinX();
        final float maxWorldX = heightMap.getRegion().getMaxX();
        final float minWorldZ = heightMap.getRegion().getMinZ();
        final float maxWorldZ = heightMap.getRegion().getMaxZ();
        final float terrainBlockSize_m = ( heightMap.getRegion().getWidth_m() + heightMap.getRegion().getDepth_m() ) / 2.0f;
        final int sizeX_grids = heightMap.getWidth_grids();
        final int sizeZ_grids = heightMap.getDepth_grids();
        final FloatBuffer vertices = terrainMesh.getVertexBuffer( 0 );
        final FloatBuffer normals = terrainMesh.getNormalBuffer( 0 );
        float gridWidth_m = heightMap.getRegion().getWidth_m() / heightMap.getWidth_grids();
        float gridDepth_m = heightMap.getRegion().getDepth_m() / heightMap.getDepth_grids();

        // Fill in the vertex height data for the mesh from the provided data
        for ( int z = 0; z < sizeZ_grids; z++ )
        {
            for ( int x = 0; x < sizeX_grids; x++ )
            {
                // Calculate x and z position
                final float xPos = MathUtils.interpolate( x, 0, sizeX_grids - 1, minWorldX, maxWorldX );
                final float zPos = MathUtils.interpolate( z, 0, sizeZ_grids - 1, minWorldZ, maxWorldZ );

                // Read height
                float height = heightMap.getValueAt( x, z );

                // Compute normal
                heightMap.calculateNormal( x, z, theTempLandscapeNormal );

                // Smooth to parent height along marigin edges
                float smoothingFactor = calculateSmoothingFactor( sizeX_grids, sizeZ_grids, x, z );
                if ( smoothingFactor > 0 && parentBuffer != null )
                {
                    // Smooth height
                    float parentHeight = parentBuffer.getWorldValueAt( xPos, zPos );
                    height = MathUtils.interpolate( smoothingFactor, height, parentHeight );

                    // Smooth normal
                    parentBuffer.calculateNormalAtWorldPos( xPos, zPos, theTempParentNormal );
                    theTempLandscapeNormal[ 0 ] = MathUtils.interpolate( smoothingFactor,
                                                                         theTempLandscapeNormal[ 0 ],
                                                                         theTempParentNormal[ 0 ] );
                    theTempLandscapeNormal[ 1 ] = MathUtils.interpolate( smoothingFactor,
                                                                         theTempLandscapeNormal[ 1 ],
                                                                         theTempParentNormal[ 1 ] );
                    theTempLandscapeNormal[ 2 ] = MathUtils.interpolate( smoothingFactor,
                                                                         theTempLandscapeNormal[ 2 ],
                                                                         theTempParentNormal[ 2 ] );
                }

                // Copy data to the target buffers
                int bufferIndex = ( x + z * ( sizeX_grids ) ) * 3; // 3 floats per vertex
                vertices.put( bufferIndex, xPos );
                vertices.put( bufferIndex + 1, height );
                vertices.put( bufferIndex + 2, zPos );

                normals.put( bufferIndex, theTempLandscapeNormal[ 0 ] );
                normals.put( bufferIndex + 1, theTempLandscapeNormal[ 1 ] );
                normals.put( bufferIndex + 2, theTempLandscapeNormal[ 2 ] );

            }
        }

        // Tell JME to update the terrain mesh
        terrainMesh.updateGeometricState( 0, true );

        // Update bounding box
        terrainMesh.updateModelBound();
    }


    /* *
     * Update the terrain texture offsets to make the terrain texture scroll with the terrain.
     *
     * @param terrainMesh the mesh to update
     * @param width_grids
     * @param depth_grids
     * /
    public void updateTextureOffset( final TriMesh terrainMesh,
                                     final float centerX,
                                     final float centerZ,
                                     final int width_grids,
                                     final int depth_grids,
                                     final float width_m,
                                     final float depth_m )
    {
        // Check parameters
        if ( terrainMesh == null )
        {
            throw new IllegalArgumentException( "The parameter terrainMesh should not be null." );
        }

        if ( width_m <= 0 )
        {
            throw new IllegalArgumentException( "The parameter width_m should be larger than " + 0 + ", but it was: " + width_m );
        }

        if ( depth_m <= 0 )
        {
            throw new IllegalArgumentException( "The parameter depth_m should be larger than " + 0 + ", but it was: " + depth_m );
        }

        final FloatBuffer textureCoordinates = terrainMesh.getTextureBuffer( 0, 0 );

        for ( int z = 0; z < depth_grids; z++ )
        {
            for ( int x = 0; x < width_grids; x++ )
            {
                // Calculate new texture world x and z position
/*
                final float xTexturePos = centerX / width_m + MathUtils.interpolate( x, 0, width_grids - 1, -0.5f, 0.5f );
                final float zTexturePos = centerZ / depth_m + MathUtils.interpolate( z, 0, depth_grids - 1, -0.5f, 0.5f );
* /
                final float xTexturePos = centerX + width_m * MathUtils.interpolate( x,
                                                                                     0,
                                                                                     width_grids - 1,
                                                                                     -0.5f,
                                                                                     0.5f );
                final float zTexturePos = centerZ + depth_m * MathUtils.interpolate( z,
                                                                                     0,
                                                                                     depth_grids - 1,
                                                                                     -0.5f,
                                                                                     0.5f );

                // Copy data to the target buffer
                int textureBufferIndex = ( x + z * ( width_grids ) ) * 2; // 2 floats per texture coordinate
                textureCoordinates.put( textureBufferIndex, xTexturePos );
                textureCoordinates.put( textureBufferIndex + 1, zTexturePos );
            }
        }

        // Tell JME to update the terrain mesh
        terrainMesh.updateGeometricState( 0, true );

        // CHECK: Is this needed?
        terrainMesh.updateRenderState();
    }
* /

    / **
     * Update the mesh to be hollow or solid or empty by modifying the mesh indexes
     *
     * @param triMesh
     * @param hollow
     * @param visible
     * @param width_grids
     * @param depth_grids
     * @param holeOffsetX offset of the hole from the center
     * @param holeOffsetZ offset of the hole from the center
     * /
    public void updateMeshIndexes( final TriMesh triMesh,
                                   final boolean hollow,
                                   final boolean visible,
                                   final int width_grids,
                                   final int depth_grids,
                                   final int holeOffsetX,
                                   final int holeOffsetZ )
    {
        final int[] indices;
        if ( visible )
        {
            // Stich together the points into triangles
            // REFACTOR: Pass an int buffer into the method instead..
            indices = initializeIndices( width_grids, depth_grids, hollow, holeOffsetX, holeOffsetZ );
        }
        else
        {
            // This mesh should not be visible..
            indices = new int[]{ };
        }

        // Replace old indexes
        // OPTIMIZE: Just keep the two sets in two memory arrays...  No need to recreate every time, or to store separate for each LOD
        triMesh.setIndexBuffer( 0, BufferUtils.createIntBuffer( indices ) );
    }

    //======================================================================
    // Private Methods

*/
    /* *
     * @return how much to smooth a value at the specified position, inside a rectangle of the specified size. Ranges
     *         from 0 (no smooting), to 1 (use 100% smoothed value)
     * /
    private float calculateSmoothingFactor( final int sizeX_grids,
                                            final int sizeZ_grids,
                                            final int x,
                                            final int z )
    {
        float smoothX = calculateSmoothingAlongAxis( sizeX_grids - 1, x );
        float smoothZ = calculateSmoothingAlongAxis( sizeZ_grids - 1, z );

        return Math.max( smoothX, smoothZ );
    }


    private float calculateSmoothingAlongAxis( final int size_squares, final int p )
    {
        float smoothBoundary = size_squares * SMOOTHING_MARGIN_SIZE_PERCENT;

        float smooth = 0;
        smooth = Math.max( smooth, smoothBoundary - p );
        smooth = Math.max( smooth, p - ( size_squares - smoothBoundary ) );

        smooth /= smoothBoundary;

        return smooth;
    }
*/

    private void layOutVetices()
    {
        for ( int y = 0; y <= mySizeY_cells; y++ )
        {
            for ( int x = 0; x <= mySizeX_cells; x++ )
            {
                prepareTerrainMeshEntryData( x, y );
            }
        }

/*
        // Down-folded edges
        for ( int x = 0; x < sizeZ_grids + 2; x++ )
        {
            prepareTerrainMeshEntryData( x, 0, vertexes, colors, textureCoordinates, sizeX_grids, sizeZ_grids, region );
            prepareTerrainMeshEntryData( x, sizeZ_grids + 1, vertexes, colors, textureCoordinates, sizeX_grids, sizeZ_grids,
                                         region );
        }
        for ( int z = 1; z < sizeZ_grids + 1; z++ )
        {
            prepareTerrainMeshEntryData( 0, z, vertexes, colors, textureCoordinates, sizeX_grids, sizeZ_grids, region );
            prepareTerrainMeshEntryData( sizeX_grids + 1, z, vertexes, colors, textureCoordinates, sizeX_grids, sizeZ_grids,
                                         region );
        }
*/
    }


    private void prepareTerrainMeshEntryData( final int x, final int z )
    {
        final float xPos = (float) MathUtils.interpolate( x, 0, mySizeX_cells, myX1, myX2 );
        final float yPos = (float) MathUtils.interpolate( z, 0, mySizeY_cells, myY1, myY2 );

        final int index = calculateMeshIndex( x, z );

        final Vector3f position = new Vector3f( xPos, yPos, myZ );
        final Vector3f normal = new Vector3f( 0, 0, 1 );
        final ColorRGBA color = new ColorRGBA( 1.0f, 1.0f, 1.0f, 1.0f );
        final Vector2f textureCoordinate = new Vector2f( ( 1.0f * x ) / mySizeX_cells,
                                                         ( 1.0f * z ) / mySizeY_cells ); // Stretch the texture across the terrain mesh

        BufferUtils.setInBuffer( position, myVertexes, index );
        BufferUtils.setInBuffer( normal, myNormals, index );
        BufferUtils.setInBuffer( color, myColors, index );
        BufferUtils.setInBuffer( textureCoordinate, myTextureCoordinates, index );
    }


    private int calculateMeshIndex( final int x, final int y )
    {
        return x + y * mySizeX_vertices;
    }


    private void initializeIndices()
    {
        // OPTIMIZE: Use triangle strips or fans to get more efficient results!

        // Create indices indicating the connections
        int index = 0;
        for ( int y = 0; y < mySizeY_vertices - 1; y++ )
        {
            for ( int x = 0; x < mySizeX_vertices - 1; x++ )
            {
                final int topLeft = x + y * mySizeX_vertices;
                final int topRight = ( x + 1 ) + y * mySizeX_vertices;
                final int bottomLeft = x + ( y + 1 ) * mySizeX_vertices;
                final int bottomRight = ( x + 1 ) + ( y + 1 ) * mySizeX_vertices;

                myIndices.put( index++, topLeft );
                myIndices.put( index++, bottomRight );
                myIndices.put( index++, topRight );
                myIndices.put( index++, bottomRight );
                myIndices.put( index++, topLeft );
                myIndices.put( index++, bottomLeft );
            }
        }
    }


    private static boolean isDownFoldedEdge( final int x, final int z, final int sizeX_grids, final int sizeZ_grids )
    {
        final HoleCheckResult xStatus = checkIfInHole( x, sizeX_grids );
        final HoleCheckResult zStatus = checkIfInHole( z, sizeZ_grids );

        // We are at the edge if we are around the hole but not in it
        return ( xStatus == HoleCheckResult.IN_HOLE && zStatus == HoleCheckResult.AT_EDGE ) ||
               ( xStatus == HoleCheckResult.AT_EDGE && zStatus == HoleCheckResult.IN_HOLE ) ||
               ( xStatus == HoleCheckResult.AT_EDGE && zStatus == HoleCheckResult.AT_EDGE );
    }


    private static boolean isInsideHole( final int x, final int z, final int sizeX_grids, final int sizeZ_grids,
                                         final int holeOffsetX, final int holeOffsetZ )
    {
        return checkIfInHole( x + holeOffsetX, sizeX_grids ) == HoleCheckResult.IN_HOLE &&
               checkIfInHole( z + holeOffsetZ, sizeZ_grids ) == HoleCheckResult.IN_HOLE;
    }


    private static HoleCheckResult checkIfInHole( int p, int size_grids )
    {
        final int holeRadius = size_grids / 4;
        final int totalRadius = size_grids / 2;

        final int startEdge = totalRadius - holeRadius;
        final int endEdge = totalRadius + holeRadius;

        boolean edge = p == startEdge - 1 || p == endEdge;
        boolean hole = p >= startEdge && p < endEdge;

        if ( hole )
        {
            return HoleCheckResult.IN_HOLE;
        }
        else if ( edge )
        {
            return HoleCheckResult.AT_EDGE;
        }
        else
        {
            return HoleCheckResult.OUTSIDE;
        }
    }

    //======================================================================
    // Inner Classes

    private enum HoleCheckResult
    {

        OUTSIDE, AT_EDGE, IN_HOLE

    }

    //======================================================================
    // Public Methods

}

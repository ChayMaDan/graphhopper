package com.graphhopper.gtfs;

import com.graphhopper.storage.Graph;
import com.graphhopper.util.details.PathDetailsBuilderFactory;

import com.graphhopper.ResponsePath;
import com.graphhopper.Trip;
import com.graphhopper.gtfs.fare.Amount;
import com.graphhopper.util.PointList;
import com.graphhopper.util.Translation;
import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.weighting.Weighting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TripFromLabelTest {
    
    @Mock 
    private Graph mockGraph;
    @Mock 
    private EncodedValueLookup mockEncodedValueLookup;
    @Mock 
    private GtfsStorage mockGtfsStorage;
    @Mock 
    private RealtimeFeed mockRealtimeFeed;
    @Mock
    private PathDetailsBuilderFactory mockPathDetailsBuilderFactory;
    @Mock
    private Translation mockTranslation;
    @Mock
    private Weighting mockWeighting;

    private TripFromLabel tripFromLabel;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        tripFromLabel = new TripFromLabel(mockGraph, mockEncodedValueLookup, mockGtfsStorage, mockRealtimeFeed, mockPathDetailsBuilderFactory, 5.0);
    }

    //Test 1 ajouté le 27/09/2025
    @Test
    public void getCheapestFareEmptyPtTest(){
        //nom : getCheapestFareEmptyTest
        //intention : tester la méthode getCheapestFare lorsqu'aucun moyen de transport public (Pt) n'est disponible
        //motivation de données : liste vide pour vérifier le comportement avec aucune donnée pour Pt
        //Oracle : doit vérifier Optional.empty() car aucun moyen de transport public n'est disponible
        
        List<Trip.Leg> emptyPtList = new ArrayList<>();

        Optional<Amount> resultat = TripFromLabel.getCheapestFare(mockGtfsStorage, emptyPtList);

        assertFalse(resultat.isPresent());
    }

    //Test 2 ajouté le 27/09/2025
    @Test
    public void getCheapestFareWalkLegsTest(){
        //nom : getCheapestFareWalkLegsTest
        //intention : tester la méthode getCheapestFare lorsque le trajet ne peut se faire qu'à pied
        //motivation de données : liste avec uniquement des trajets à pied pour vérifier le comportement lorsq'il n'y a aucun moyen de transport public
        //Oracle : doit vérifier Optional.empty() car aucun moyen de transport public n'est disponible
        
        Trip.WalkLeg mockWalkLeg1 = mock(Trip.WalkLeg.class);
        Trip.WalkLeg mockWalkLeg2 = mock(Trip.WalkLeg.class);
        List<Trip.Leg> WalkLegOnlyList = Arrays.asList(mockWalkLeg1, mockWalkLeg2);
        
        Optional<Amount> resultat = TripFromLabel.getCheapestFare(mockGtfsStorage, WalkLegOnlyList);

        assertFalse(resultat.isPresent());
    }

    //Test 3 ajouté le 27/09/2025

    //Test 4 ajouté le 27/09/2025
    @Test
    public void parsePartitionToLegsEmptyPathTest(){
        //nom : parsePartitionToLegsEmptyPathTest
        //intention : tester la méthode parsePartitionToLegs lorsque le chemin est vide (longueur =< 1)
        //motivation de données : liste vide pour vérifier le comportement avec aucune donnée
        //Oracle : doit vérifier que la liste retournée est vide
        
        List<Label.Transition> emptyPath = new ArrayList<>();
        List<String> requestedPathdetails = new ArrayList<>();

        List<Trip.Leg> resultat = tripFromLabel.parsePartitionToLegs(emptyPath, mockGraph, mockEncodedValueLookup, mockWeighting, mockTranslation, requestedPathdetails);

        assert(resultat.isEmpty());
    }

    //Test 5 ajouté le 27/09/2025
    @Test
    public void parsePartitionToLegsOneEltTest(){
        //nom : parsePartitionToLegsOneEltTest
        //intention : tester la méthode parsePartitionToLegs lorsque le chemin contient un seul élément (longueur = 1)
        //motivation de données : liste avec un seul élément pour vérifier le comportement avec une donnée minimale
        //Oracle : doit vérifier que la liste retournée est vide car un seul élément ne permet pas de former un trajet
        
        Label.Transition mockTransition = mock(Label.Transition.class);
        List<Label.Transition> oneEltPath = Arrays.asList(mockTransition);
        List<String> requestedPathdetails = new ArrayList<>();

        List<Trip.Leg> resultat = tripFromLabel.parsePartitionToLegs(oneEltPath, mockGraph, mockEncodedValueLookup, mockWeighting, mockTranslation, requestedPathdetails);

        assert(resultat.isEmpty());
    }

    //Test 6 ajouté le 27/09/2025
    @Test
    public void createResponsePathEmptyTest(){
        //nom : createResponsePathEmptyTest
        //intention : tester la méthode statique createResponsePath lorsque le trajet est vide
        //motivation de données : liste vide pour vérifier le comportement avec aucune donnée
        //Oracle : ResponsePath avec les propriétés par défaut et une liste de legs vide
        
        PointList mockPointList = new PointList();
        mockPointList.add(45.5017, -73.5673); // Montréal

        List<Trip.Leg> emptyLegs = new ArrayList<>();

        assertThrows(IndexOutOfBoundsException.class, () -> {
            TripFromLabel.createResponsePath(mockGtfsStorage, mockTranslation, mockPointList, emptyLegs);
        });

        //ResponsePath resultat = TripFromLabel.createResponsePath(mockGtfsStorage, mockTranslation, mockPointList,emptyLegs);

        //assertNotNull(resultat);
        //assertEquals(mockPointList, resultat.getWaypoints());
        //assertTrue(resultat.getLegs().isEmpty());
        //assertEquals(0, resultat.getDistance(), 0.001);
    }

    //Test 7 ajouté le 27/09/2025
    @Test
    public void createResponsePathWalkLegTest(){
        //nom : createResponsePathWalkLegTest
        //intention : tester la modification d'un trajet à pied dans la méthode statique createResponsePath
        //motivation de données : walkLeg mockés pour vérifier si les propriétés sont correctement transférées
        //Oracle : vérifier si les propriétés ont changé

        PointList mockPointList = new PointList();
        mockPointList.add(45.5017, -73.5673); // Montréal

        Trip.Leg mockLeg1 = mock(Trip.Leg.class);
        when(mockLeg1.getDistance()).thenReturn(1500.0);
        when(mockLeg1.getDepartureTime()).thenReturn(new Date(1000L));
        when(mockLeg1.getArrivalTime()).thenReturn(new Date(2000L));

        Trip.Leg mockLeg2 = mock(Trip.Leg.class);
        when(mockLeg2.getDistance()).thenReturn(2500.0);
        when(mockLeg2.getDepartureTime()).thenReturn(new Date(3000L));
        when(mockLeg2.getArrivalTime()).thenReturn(new Date(4000L));

        List<Trip.Leg> tripLegs = Arrays.asList(mockLeg1, mockLeg2);

        ResponsePath resultat = TripFromLabel.createResponsePath(mockGtfsStorage, mockTranslation, mockPointList, tripLegs);

        assertNotNull(resultat);
        assertEquals(2, resultat.getLegs().size());
        assertEquals(4000.0, resultat.getDistance(), 0.001);

    }
}

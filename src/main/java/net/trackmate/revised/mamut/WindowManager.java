package net.trackmate.revised.mamut;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.scijava.ui.behaviour.KeyStrokeAdder;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.InputTriggerDescription;
import org.scijava.ui.behaviour.io.InputTriggerDescriptionsBuilder;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import bdv.spimdata.SpimDataMinimal;
import bdv.tools.ToggleDialogAction;
import bdv.util.AbstractNamedAction;
import bdv.viewer.RequestRepaint;
import bdv.viewer.TimePointListener;
import bdv.viewer.ViewerFrame;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;
import mpicbg.spim.data.generic.AbstractSpimData;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.listenable.GraphChangeListener;
import net.trackmate.graph.listenable.ListenableGraph;
import net.trackmate.revised.bdv.BigDataViewerMaMuT;
import net.trackmate.revised.bdv.SharedBigDataViewerData;
import net.trackmate.revised.bdv.overlay.MouseHighlightHandler;
import net.trackmate.revised.bdv.overlay.OverlayContext;
import net.trackmate.revised.bdv.overlay.OverlayGraphRenderer;
import net.trackmate.revised.bdv.overlay.RenderSettings;
import net.trackmate.revised.bdv.overlay.RenderSettings.UpdateListener;
import net.trackmate.revised.bdv.overlay.SelectionBehaviours;
import net.trackmate.revised.bdv.overlay.ui.RenderSettingsDialog;
import net.trackmate.revised.bdv.overlay.wrap.OverlayContextWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayEdgeWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayFocusWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayGraphWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayHighlightWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayNavigationWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlaySelectionWrapper;
import net.trackmate.revised.bdv.overlay.wrap.OverlayVertexWrapper;
import net.trackmate.revised.context.Context;
import net.trackmate.revised.context.ContextChooser;
import net.trackmate.revised.context.ContextListener;
import net.trackmate.revised.context.ContextProvider;
import net.trackmate.revised.model.mamut.BoundingSphereRadiusStatistics;
import net.trackmate.revised.model.mamut.Link;
import net.trackmate.revised.model.mamut.Model;
import net.trackmate.revised.model.mamut.ModelOverlayProperties;
import net.trackmate.revised.model.mamut.Spot;
import net.trackmate.revised.trackscheme.DefaultModelFocusProperties;
import net.trackmate.revised.trackscheme.DefaultModelGraphProperties;
import net.trackmate.revised.trackscheme.DefaultModelHighlightProperties;
import net.trackmate.revised.trackscheme.DefaultModelNavigationProperties;
import net.trackmate.revised.trackscheme.DefaultModelSelectionProperties;
import net.trackmate.revised.trackscheme.ModelFocusProperties;
import net.trackmate.revised.trackscheme.ModelHighlightProperties;
import net.trackmate.revised.trackscheme.ModelNavigationProperties;
import net.trackmate.revised.trackscheme.ModelSelectionProperties;
import net.trackmate.revised.trackscheme.TrackSchemeContextListener;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeSelection;
import net.trackmate.revised.trackscheme.display.TrackSchemeFrame;
import net.trackmate.revised.trackscheme.display.laf.TrackSchemeStyle;
import net.trackmate.revised.trackscheme.display.ui.TrackSchemeStylePanel.TrackSchemeStyleDialog;
import net.trackmate.revised.ui.grouping.GroupHandle;
import net.trackmate.revised.ui.grouping.GroupLocksPanel;
import net.trackmate.revised.ui.grouping.GroupManager;
import net.trackmate.revised.ui.selection.FocusListener;
import net.trackmate.revised.ui.selection.FocusModel;
import net.trackmate.revised.ui.selection.HighlightListener;
import net.trackmate.revised.ui.selection.HighlightModel;
import net.trackmate.revised.ui.selection.NavigationHandler;
import net.trackmate.revised.ui.selection.Selection;
import net.trackmate.revised.ui.selection.SelectionListener;

public class WindowManager
{
	/**
	 * Information for one BigDataViewer window.
	 */
	public static class BdvWindow
	{
		private final ViewerFrame viewerFrame;

		private final OverlayGraphRenderer< ?, ? > tracksOverlay;

		private final GroupHandle groupHandle;

		private final ContextProvider< Spot > contextProvider;

		public BdvWindow(
				final ViewerFrame viewerFrame,
				final OverlayGraphRenderer< ?, ? > tracksOverlay,
				final GroupHandle groupHandle,
				final ContextProvider< Spot > contextProvider )
		{
			this.viewerFrame = viewerFrame;
			this.tracksOverlay = tracksOverlay;
			this.groupHandle = groupHandle;
			this.contextProvider = contextProvider;
		}

		public ViewerFrame getViewerFrame()
		{
			return viewerFrame;
		}

		public OverlayGraphRenderer< ?, ? > getTracksOverlay()
		{
			return tracksOverlay;
		}

		public GroupHandle getGroupHandle()
		{
			return groupHandle;
		}

		public ContextProvider< Spot > getContextProvider()
		{
			return contextProvider;
		}
	}

	/**
	 * Information for one TrackScheme window.
	 */
	public static class TsWindow
	{
		private final TrackSchemeFrame trackSchemeFrame;

		private final GroupHandle groupHandle;

		private final ContextChooser< Spot > contextChooser;

		public TsWindow(
				final TrackSchemeFrame trackSchemeFrame,
				final GroupHandle groupHandle,
				final ContextChooser< Spot > contextChooser )
		{
			this.trackSchemeFrame = trackSchemeFrame;
			this.groupHandle = groupHandle;
			this.contextChooser = contextChooser;
		}

		public TrackSchemeFrame getTrackSchemeFrame()
		{
			return trackSchemeFrame;
		}

		public GroupHandle getGroupHandle()
		{
			return groupHandle;
		}

		public ContextChooser< Spot > getContextChooser()
		{
			return contextChooser;
		}
	}

	/**
	 * TODO!!! related to {@link OverlayContextWrapper}
	 *
	 * @param <V>
	 *
	 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
	 */
	public static class BdvContextAdapter< V > implements ContextListener< V >, ContextProvider< V >
	{
		private final String contextProviderName;

		private final ArrayList< ContextListener< V > > listeners;

		private Context< V > context;

		public BdvContextAdapter( final String contextProviderName )
		{
			this.contextProviderName = contextProviderName;
			listeners = new ArrayList<>();
		}

		@Override
		public String getContextProviderName()
		{
			return contextProviderName;
		}

		@Override
		public synchronized boolean addContextListener( final ContextListener< V > l )
		{
			if ( !listeners.contains( l ) )
			{
				listeners.add( l );
				l.contextChanged( context );
				return true;
			}
			return false;
		}

		@Override
		public synchronized boolean removeContextListener( final ContextListener< V > l )
		{
			return listeners.remove( l );
		}

		@Override
		public synchronized void contextChanged( final Context< V > context )
		{
			this.context = context;
			for ( final ContextListener< V > l : listeners )
				l.contextChanged( context );
		}
	}

	private final Model model;

	private final InputTriggerConfig keyconf;

	private final GroupManager groupManager;

	private final SharedBigDataViewerData sharedBdvData;

	private final int minTimepoint;

	private final int maxTimepoint;

	private final Selection< Spot, Link > selection;

	private final HighlightModel< Spot, Link > highlightModel;

	private final FocusModel< Spot, Link > focusModel;

	private final BoundingSphereRadiusStatistics radiusStats;

	/**
	 * All currently open BigDataViewer windows.
	 */
	private final List< BdvWindow > bdvWindows = new ArrayList<>();

	/**
	 * The {@link ContextProvider}s of all currently open BigDataViewer windows.
	 */
	private final List< ContextProvider< Spot > > contextProviders = new ArrayList<>();

	/**
	 * All currently open TrackScheme windows.
	 */
	private final List< TsWindow > tsWindows = new ArrayList<>();

	public WindowManager(
			final String spimDataXmlFilename,
			final SpimDataMinimal spimData,
			final Model model,
			final InputTriggerConfig keyconf )
	{
		this.model = model;
		this.keyconf = keyconf;

		groupManager = new GroupManager();
		final RequestRepaint requestRepaint = new RequestRepaint()
		{
			@Override
			public void requestRepaint()
			{
				for ( final BdvWindow w : bdvWindows )
					w.getViewerFrame().getViewerPanel().requestRepaint();
			}
		};
		sharedBdvData = new SharedBigDataViewerData( spimDataXmlFilename, spimData, ViewerOptions.options().inputTriggerConfig( keyconf ), requestRepaint );

		final ListenableGraph< Spot, Link > graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();
		selection = new Selection<>( graph, idmap );
		highlightModel = new HighlightModel< Spot, Link  >( idmap );
		radiusStats = new BoundingSphereRadiusStatistics( model );
		focusModel = new FocusModel<>( idmap );

		minTimepoint = 0;
		maxTimepoint = sharedBdvData.getNumTimepoints() - 1;
		/*
		 * TODO: (?) For now, we use timepoint indices in MaMuT model, instead
		 * of IDs/names. This is because BDV also displays timepoint index, and
		 * it would be confusing to have different labels in TrackScheme. If
		 * this is changed in the future, then probably only in the model files.
		 */
	}

	private synchronized void addBdvWindow( final BdvWindow w )
	{
		w.getViewerFrame().addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				removeBdvWindow( w );
			}
		} );
		bdvWindows.add( w );
		contextProviders.add( w.getContextProvider() );
		for ( final TsWindow tsw : tsWindows )
			tsw.getContextChooser().updateContextProviders( contextProviders );
	}

	private synchronized void removeBdvWindow( final BdvWindow w )
	{
		bdvWindows.remove( w );
		contextProviders.remove( w.getContextProvider() );
		for ( final TsWindow tsw : tsWindows )
			tsw.getContextChooser().updateContextProviders( contextProviders );
	}

	private synchronized void addTsWindow( final TsWindow w )
	{
		w.getTrackSchemeFrame().addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				removeTsWindow( w );
			}
		} );
		tsWindows.add( w );
		w.getContextChooser().updateContextProviders( contextProviders );
	}

	private synchronized void removeTsWindow( final TsWindow w )
	{
		tsWindows.remove( w );
		w.getContextChooser().updateContextProviders( new ArrayList<>() );
	}

	// TODO
	private int bdvName = 1;

	public void createBigDataViewer()
	{
		final GroupHandle bdvGroupHandle = groupManager.createGroupHandle();

		final OverlayGraphWrapper< Spot, Link > overlayGraph = new OverlayGraphWrapper<>(
				model.getGraph(),
				model.getGraphIdBimap(),
				model.getSpatioTemporalIndex(),
				new ModelOverlayProperties( radiusStats, selection ) );

		final OverlayHighlightWrapper< Spot, Link > overlayHighlight = new OverlayHighlightWrapper<>(
				model.getGraphIdBimap(),
				highlightModel );

		final OverlayFocusWrapper< Spot, Link > overlayFocus = new OverlayFocusWrapper<>(
				model.getGraphIdBimap(),
				focusModel );

		final OverlaySelectionWrapper< Spot, Link > overlaySelection = new OverlaySelectionWrapper<>(
				selection );

		final String windowTitle = "BigDataViewer " + (bdvName++); // TODO: use JY naming scheme
		final BigDataViewerMaMuT bdv = BigDataViewerMaMuT.open( sharedBdvData, windowTitle );
		final ViewerFrame viewerFrame = bdv.getViewerFrame();
		final ViewerPanel viewer = bdv.getViewer();


		// TODO: It's ok to create the wrappers here, but wiring up Listeners should be done elsewhere


//		if ( !bdv.tryLoadSettings( bdvFile ) ) // TODO
//			InitializeViewerState.initBrightness( 0.001, 0.999, bdv.getViewer(), bdv.getSetupAssignments() );

		viewer.setTimepoint( currentTimepoint );
		final OverlayGraphRenderer< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > tracksOverlay = new OverlayGraphRenderer<>(
				overlayGraph,
				overlayHighlight,
				overlayFocus );
		viewer.getDisplay().addOverlayRenderer( tracksOverlay );
		viewer.addRenderTransformListener( tracksOverlay );
		viewer.addTimePointListener( tracksOverlay );
		overlayHighlight.addHighlightListener( new HighlightListener()
		{
			@Override
			public void highlightChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );
		overlayFocus.addFocusListener( new FocusListener()
		{
			@Override
			public void focusChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );
		model.getGraph().addGraphChangeListener( new GraphChangeListener()
		{
			@Override
			public void graphChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );
		overlaySelection.addSelectionListener( new SelectionListener()
		{
			@Override
			public void selectionChanged()
			{
				viewer.getDisplay().repaint();
			}
		} );

		final MouseHighlightHandler< ?, ? > highlightHandler = new MouseHighlightHandler<>( overlayGraph, tracksOverlay, overlayHighlight );
		viewer.getDisplay().addHandler( highlightHandler );
		viewer.addRenderTransformListener( highlightHandler );

		final NavigationHandler< Spot > navigationHandler = new NavigationHandler<>( bdvGroupHandle );
		final OverlayNavigationWrapper< Spot, Link > navigation =
				new OverlayNavigationWrapper< Spot, Link >( viewer, overlayGraph, navigationHandler );
		final SelectionBehaviours< ?, ? > selectionBehaviours = new SelectionBehaviours<>( overlayGraph, tracksOverlay, overlaySelection, navigation );
		selectionBehaviours.installBehaviourBindings( viewerFrame.getTriggerbindings(), keyconf );

		final OverlayContext< OverlayVertexWrapper< Spot, Link > > overlayContext = new OverlayContext<>( overlayGraph, tracksOverlay );
		viewer.addRenderTransformListener( overlayContext );
		final BdvContextAdapter< Spot > contextProvider = new BdvContextAdapter<>( windowTitle );
		final OverlayContextWrapper< Spot, Link > overlayContextWrapper = new OverlayContextWrapper<>(
				overlayContext,
				contextProvider );

		final GroupLocksPanel lockPanel = new GroupLocksPanel( bdvGroupHandle );
		viewerFrame.add( lockPanel, BorderLayout.NORTH );
		viewerFrame.pack();

		/*
		 * TODO: this is still wrong. There should be one central entity syncing
		 * time for several BDV frames and TrackSchemePanel should listen to
		 * that. Ideally windows should be configurable to "share" timepoints or
		 * not.
		 */
		viewer.addTimePointListener( tpl );
//		viewer.repaint(); // TODO remove?


		// TODO revise
		// RenderSettingsDialog triggered by "R"
		final RenderSettings renderSettings = new RenderSettings(); // TODO should be in overlay eventually
		final String RENDER_SETTINGS = "render settings";
		final RenderSettingsDialog renderSettingsDialog = new RenderSettingsDialog( viewerFrame, renderSettings );
		final ActionMap actionMap = new ActionMap();
		AbstractNamedAction.put( actionMap, new ToggleDialogAction( RENDER_SETTINGS, renderSettingsDialog ) );
		final InputMap inputMap = new InputMap();
		final KeyStrokeAdder a = keyconf.keyStrokeAdder( inputMap, "mamut" );
		a.put( RENDER_SETTINGS, "R" );
		viewerFrame.getKeybindings().addActionMap( "mamut", actionMap );
		viewerFrame.getKeybindings().addInputMap( "mamut", inputMap );
		renderSettings.addUpdateListener( new UpdateListener()
		{
			@Override
			public void renderSettingsChanged()
			{
				tracksOverlay.setRenderSettings( renderSettings );
				// TODO: less hacky way of triggering repaint and context update
				viewer.repaint();
				overlayContextWrapper.contextChanged( overlayContext );
			}
		} );

		final BdvWindow bdvWindow = new BdvWindow( viewerFrame, tracksOverlay, bdvGroupHandle, contextProvider );
		addBdvWindow( bdvWindow );
	}

	// TODO testing only
	private int currentTimepoint = 0;

	// TODO testing only
	private final TimePointListener tpl = new TimePointListener()
	{
		@Override
		public void timePointChanged( final int timePointIndex )
		{
			if ( currentTimepoint != timePointIndex )
			{
				currentTimepoint = timePointIndex;
				for ( final TsWindow w : tsWindows )
					w.getTrackSchemeFrame().getTrackschemePanel().timePointChanged( timePointIndex );
			}
		}
	};

	public void createTrackScheme()
	{
		final ListenableGraph< Spot, Link > graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();

		/*
		 * TrackSchemeGraph listening to model
		 */
		final DefaultModelGraphProperties< Spot, Link > properties = new DefaultModelGraphProperties<>( graph, idmap, selection );
		final TrackSchemeGraph< Spot, Link > trackSchemeGraph = new TrackSchemeGraph<>( graph, idmap, properties );

		/*
		 * TrackSchemeHighlight wrapping HighlightModel
		 */
		final ModelHighlightProperties highlightProperties = new DefaultModelHighlightProperties< Spot, Link >( graph, idmap, highlightModel );
		final TrackSchemeHighlight trackSchemeHighlight = new TrackSchemeHighlight( highlightProperties, trackSchemeGraph );

		/*
		 * TrackScheme selection
		 */
		final ModelSelectionProperties selectionProperties = new DefaultModelSelectionProperties< Spot, Link >( graph, idmap, selection );
		final TrackSchemeSelection trackSchemeSelection = new TrackSchemeSelection( selectionProperties );

		/*
		 * TrackScheme GroupHandle
		 */
		final GroupHandle groupHandle = groupManager.createGroupHandle();

		/*
		 * TrackScheme navigation
		 */
		final NavigationHandler< Spot > navigationHandler = new NavigationHandler< Spot >( groupHandle );
		final ModelNavigationProperties navigationProperties = new DefaultModelNavigationProperties< Spot, Link >( graph, idmap, navigationHandler );
		final TrackSchemeNavigation trackSchemeNavigation = new TrackSchemeNavigation( navigationProperties, trackSchemeGraph );

		/*
		 * TrackScheme focus
		 */
		final ModelFocusProperties focusProperties = new DefaultModelFocusProperties<>( graph, idmap, focusModel );
		final TrackSchemeFocus trackSchemeFocus = new TrackSchemeFocus( focusProperties, trackSchemeGraph );

		/*
		 * TrackScheme ContextChooser
		 */
		final TrackSchemeContextListener< Spot > contextListener = new TrackSchemeContextListener< >(
				idmap,
				trackSchemeGraph );
		final ContextChooser< Spot > contextChooser = new ContextChooser<>( contextListener );

		/*
		 * show TrackSchemeFrame
		 */
		final TrackSchemeFrame frame = new TrackSchemeFrame(
				trackSchemeGraph,
				trackSchemeHighlight,
				trackSchemeFocus,
				trackSchemeSelection,
				trackSchemeNavigation,
				groupHandle,
				contextChooser );
		frame.getTrackschemePanel().setTimepointRange( minTimepoint, maxTimepoint );
		frame.getTrackschemePanel().graphChanged();
		contextListener.setContextListener( frame.getTrackschemePanel() );
		frame.setVisible( true );

		// TODO revise
		// TrackSchemeStyleDialog triggered by "R"
		final TrackSchemeStyle style = TrackSchemeStyle.defaultStyle();
		final String TRACK_SCHEME_STYLE_SETTINGS = "render settings";
		final TrackSchemeStyleDialog trackSchemeStyleDialog = new TrackSchemeStyleDialog( frame, style );
		final ActionMap actionMap = new ActionMap();
		AbstractNamedAction.put( actionMap, new ToggleDialogAction( TRACK_SCHEME_STYLE_SETTINGS, trackSchemeStyleDialog ) );
		final InputMap inputMap = new InputMap();
		final KeyStrokeAdder a = keyconf.keyStrokeAdder( inputMap, "mamut" );
		a.put( TRACK_SCHEME_STYLE_SETTINGS, "R" );
		frame.getKeybindings().addActionMap( "mamut", actionMap );
		frame.getKeybindings().addInputMap( "mamut", inputMap );
		style.addUpdateListener( new TrackSchemeStyle.UpdateListener()
		{
			@Override
			public void trackSchemeStyleChanged()
			{
				frame.getTrackschemePanel().setTrackSchemeStyle( style );
				// TODO: less hacky way of triggering repaint
				frame.getTrackschemePanel().repaint();
			}
		} );

		final TsWindow tsWindow = new TsWindow( frame, groupHandle, contextChooser );
		addTsWindow( tsWindow );
	}

	public void closeAllWindows()
	{
		final ArrayList< JFrame > frames = new ArrayList<>();
		for ( final BdvWindow w : bdvWindows )
			frames.add( w.getViewerFrame() );
		for ( final TsWindow w : tsWindows )
			frames.add( w.getTrackSchemeFrame() );
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				for ( final JFrame f : frames )
					f.dispatchEvent( new WindowEvent( f, WindowEvent.WINDOW_CLOSING ) );
			}
		} );
	}

	public Model getModel()
	{
		return model;
	}

	public AbstractSpimData< ? > getSpimData()
	{
		return sharedBdvData.getSpimData();
	}


	// TODO: move somewhere else. make bdvWindows, tsWindows accessible.
	public static class DumpInputConfig
	{
		private static List< InputTriggerDescription > buildDescriptions( final WindowManager wm ) throws IOException
		{
			final InputTriggerDescriptionsBuilder builder = new InputTriggerDescriptionsBuilder();

			final ViewerFrame viewerFrame = wm.bdvWindows.get( 0 ).viewerFrame;
			builder.addMap( viewerFrame.getKeybindings().getConcatenatedInputMap(), "bdv" );
			builder.addMap( viewerFrame.getTriggerbindings().getConcatenatedInputTriggerMap(), "bdv" );

			final TrackSchemeFrame trackschemeFrame = wm.tsWindows.get( 0 ).trackSchemeFrame;
			builder.addMap( trackschemeFrame.getKeybindings().getConcatenatedInputMap(), "ts" );
			builder.addMap( trackschemeFrame.getTriggerbindings().getConcatenatedInputTriggerMap(), "ts" );

			return builder.getDescriptions();
		}

		public static boolean mkdirs( final String fileName )
		{
			final File dir = new File( fileName ).getParentFile();
			return dir == null ? false : dir.mkdirs();
		}

		public static void writeToYaml( final String fileName, final WindowManager wm ) throws IOException
		{
			mkdirs( fileName );
			YamlConfigIO.write(  buildDescriptions( wm ), fileName );
		}
	}
}

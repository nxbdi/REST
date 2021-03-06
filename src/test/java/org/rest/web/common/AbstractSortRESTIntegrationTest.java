package org.rest.web.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.rest.client.template.IRESTTemplate;
import org.rest.common.IEntity;
import org.rest.common.util.QueryUtil;
import org.rest.test.AbstractRESTIntegrationTest;
import org.rest.testing.security.AuthenticationUtil;
import org.rest.util.SearchField;
import org.rest.util.order.OrderById;
import org.springframework.data.domain.Sort;

import com.google.common.collect.Ordering;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

public abstract class AbstractSortRESTIntegrationTest< T extends IEntity > extends AbstractRESTIntegrationTest{
	
	// tests
	
	// GET (paged)
	
	@Test
	public final void whenResourcesAreRetrievedSorted_thenNoExceptions(){
		givenAuthenticated().get( getURI() + QueryUtil.QUESTIONMARK + "page=0&size=41&sortBy=name" );
	}
	
	@Test
	public final void whenResourcesAreRetrievedPagedAndSorted_then200IsReceived(){
		final Response response = givenAuthenticated().get( getURI() + QueryUtil.QUESTIONMARK + "page=0&size=1&sortBy=name" + QueryUtil.S_ORDER_ASC );
		
		assertThat( response.getStatusCode(), is( 200 ) );
	}
	
	@Test
	public final void whenResourcesAreRetrievedSorted_then200IsReceived(){
		final Response response = givenAuthenticated().get( getURI() + QueryUtil.QUESTIONMARK + "sortBy=name" + QueryUtil.S_ORDER_ASC );
		
		assertThat( response.getStatusCode(), is( 200 ) );
	}
	
	@SuppressWarnings( "unchecked" )
	@Test
	// TODO
	@Ignore( "temporarily ignored - in progress" )
	public final void whenResourcesAreRetrievedSorted_thenResourcesAreIndeedOrdered(){
		getAPI().createAsResponse( getAPI().createNewEntity() );
		getAPI().createAsResponse( getAPI().createNewEntity() );
		
		// When
		final Response response = givenAuthenticated().get( getURI() + QueryUtil.QUESTIONMARK + "page=0&size=4&sortBy=name" + QueryUtil.S_ORDER_ASC );
		final List< T > resourcesPagedAndSorted = getAPI().getMarshaller().decode( response.asString(), List.class );
		
		// Then
		assertTrue( getOrdering().isOrdered( resourcesPagedAndSorted ) );
	}
	
	@SuppressWarnings( "unchecked" )
	@Test
	@Ignore( "not necessarily true" )
	public final void whenResourcesAreRetrievedNotSorted_thenResourcesAreNotOrdered(){
		getAPI().createAsResponse( getAPI().createNewEntity() );
		getAPI().createAsResponse( getAPI().createNewEntity() );
		
		// When
		final Response response = givenAuthenticated().get( getURI() + QueryUtil.QUESTIONMARK + "page=0&size=6" );
		final List< T > resourcesPagedAndSorted = getAPI().getMarshaller().decode( response.asString(), List.class );
		
		// Then
		assertFalse( getOrdering().isOrdered( resourcesPagedAndSorted ) );
	}
	
	@Test
	public final void whenResourcesAreRetrievedByInvalidSorting_then400IsReceived(){
		// When
		final Response response = givenAuthenticated().get( getURI() + QueryUtil.QUESTIONMARK + "page=0&size=4&sortBy=invalid" );
		
		// Then
		assertThat( response.getStatusCode(), is( 400 ) );
	}
	
	// find - check order
	
	@Test
	public final void whenResourcesAreRetrievedSortedDescById_thenNoExceptions(){
		getAPI().findAll( SearchField.id.toString(), Sort.Direction.DESC.name() );
	}
	@Test
	public final void whenResourcesAreRetrievedSortedAscById_thenResultsAreOrderedCorrectly(){
		final List< T > resourcesOrderedById = getAPI().findAll( SearchField.id.toString(), Sort.Direction.ASC.name() );
		
		assertTrue( new OrderById< T >().isOrdered( resourcesOrderedById ) );
	}
	@Test
	public final void whenResourcesAreRetrievedSortedDescById_thenResultsAreOrderedCorrectly(){
		final List< T > resourcesOrderedById = getAPI().findAll( SearchField.id.toString(), Sort.Direction.DESC.name() );
		
		assertTrue( new OrderById< T >().reverse().isOrdered( resourcesOrderedById ) );
	}
	
	// util
	
	protected final RequestSpecification givenAuthenticated(){
		return AuthenticationUtil.givenBasicAuthenticated();
	}
	
	// template method
	
	protected abstract IRESTTemplate< T > getAPI();
	
	protected abstract Ordering< T > getOrdering();
	
	protected abstract String getURI();
	
	protected abstract T createNewEntity();
	
}

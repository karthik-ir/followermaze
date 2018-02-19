/**
 * 
 */
package com.scloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.scloud.followermaze.Helper;
import com.scloud.followermaze.exception.BadInputException;
import com.scloud.followermaze.model.EventData;
import com.scloud.followermaze.model.EventTypes;
import com.scloud.followermaze.model.UserData;

/**
 * @author karthik
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FollowerMazeHelper {

	/**
	 * Test method for
	 * {@link com.scloud.followermaze.Helper#processInputLine(com.scloud.followermaze.model.EventData)}.
	 * 
	 * @throws BadInputException
	 */
	@Test(expected = BadInputException.class)
	public void testProcessInputLineWithNullInput() throws BadInputException {
		new Helper().processInputLine(null);
	}

	@Test(expected = BadInputException.class)
	public void testProcessInputLineWithEmptyInput() throws BadInputException {
		new Helper().processInputLine("");
	}

	@Test(expected = BadInputException.class)
	public void testProcessInputLineWithBadInput() throws BadInputException {
		new Helper().processInputLine("this is a dummy input");
	}

	@Test(expected = BadInputException.class)
	public void testProcessInputLineWithNonExistingEvent() throws BadInputException {
		new Helper().processInputLine("123|G|2|3");
	}

	@Test
	public void testProcessInputLineWithCorrectValues() throws BadInputException {
		EventData result = new Helper().processInputLine("97693|F|898|957");

		// Check Follow
		assertEquals(EventTypes.FOLLOW, result.getEventType());
		assertEquals(new Long(97693), result.getMessageNumber());
		assertEquals("898", result.getFromUserId());
		assertEquals("957", result.getToUserId());

		// Check Status
		result = new Helper().processInputLine("97925|S|68");
		assertEquals(EventTypes.STATUS_UPDATE, result.getEventType());
		assertEquals(new Long(97925), result.getMessageNumber());
		assertEquals("68", result.getFromUserId());
		assertNull(result.getToUserId());

		// Check Boradcast
		result = new Helper().processInputLine("93849|B");
		assertEquals(EventTypes.BROADCAST, result.getEventType());
		assertEquals(new Long(93849), result.getMessageNumber());
		assertNull(result.getFromUserId());
		assertNull(result.getToUserId());

		// Check Unfollow
		result = new Helper().processInputLine("97693|U|898|957");
		assertEquals(EventTypes.UNFOLLOW, result.getEventType());
		assertEquals(new Long(97693), result.getMessageNumber());
		assertEquals("898", result.getFromUserId());
		assertEquals("957", result.getToUserId());

		// Check Private Msg
		result = new Helper().processInputLine("97693|P|898|957");
		assertEquals(EventTypes.PRIVATE, result.getEventType());
		assertEquals(new Long(97693), result.getMessageNumber());
		assertEquals("898", result.getFromUserId());
		assertEquals("957", result.getToUserId());

	}

	/**
	 * Test method for
	 * {@link com.scloud.followermaze.Helper#readValueFromInputStream(java.net.Socket)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testReadValueFromInputStream() throws IOException {
		InputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());
		assertEquals("test data", new Helper().readValueFromInputStream(anyInputStream));
	}

	/**
	 * Test method for
	 * {@link com.scloud.followermaze.Helper#notifyClient(com.scloud.followermaze.model.UserData, com.scloud.followermaze.model.EventData)}.
	 * 
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws BadInputException
	 */
	@Test
	public void testCheckIfEventValidAndNotify() throws UnknownHostException, IOException, BadInputException {

		Helper helperMock = spy(Helper.class);

		UserData user898 = new UserData(null);
		user898.setUserId("898");

		UserData user957 = new UserData(null);
		user957.setUserId("957");
		EventData eventdata;
		boolean isNotified;
		follow(helperMock, user898, "97693|F|898|957");

		// Checking Unfollow
		eventdata = helperMock.processInputLine("97693|U|898|957");
		assertEquals(user898.getFollows().size(), 1);
		isNotified = helperMock.notifyClient(user898, eventdata);
		assertEquals(user898.getFollows().size(), 0);
		assertTrue(!isNotified);

		doReturn(true).when(helperMock).send(any(EventData.class), any(UserData.class));

		// Checking private

		// Is notified to the to user
		eventdata = helperMock.processInputLine("97693|P|957|898");
		isNotified = helperMock.notifyClient(user898, eventdata);
		assertTrue(isNotified);

		// Is not notified by to from user
		eventdata = helperMock.processInputLine("97693|P|957|898");
		isNotified = helperMock.notifyClient(user957, eventdata);
		assertTrue(!isNotified);

		// Boradcast
		eventdata = helperMock.processInputLine("93849|B");
		isNotified = helperMock.notifyClient(user957, eventdata);
		assertTrue(isNotified);
		isNotified = helperMock.notifyClient(user898, eventdata);
		assertTrue(isNotified);

		follow(helperMock, user898, "97693|F|898|957");
		
		// StatusUpdate
		eventdata = helperMock.processInputLine("97925|S|957");
		//Not notified to sender
		isNotified = helperMock.notifyClient(user957, eventdata);
		assertTrue(!isNotified);
		
		//Notified to the follwer
		isNotified = helperMock.notifyClient(user898, eventdata);
		assertTrue(isNotified);
	}

	private void follow(Helper helperMock, UserData user, String inputLine) throws BadInputException, IOException {
		// Checking Follow
		EventData eventdata = helperMock.processInputLine(inputLine);
		assertEquals(user.getFollows().size(), 0);
		boolean isNotified = helperMock.notifyClient(user, eventdata);
		assertEquals(user.getFollows().size(), 1);
		assertTrue(!isNotified);
	}
}

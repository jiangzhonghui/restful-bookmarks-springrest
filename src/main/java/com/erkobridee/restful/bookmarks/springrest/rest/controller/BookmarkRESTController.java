package com.erkobridee.restful.bookmarks.springrest.rest.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.erkobridee.restful.bookmarks.springrest.persistence.dao.IBookmarkDAO;
import com.erkobridee.restful.bookmarks.springrest.persistence.entity.Bookmark;
import com.erkobridee.restful.bookmarks.springrest.persistence.entity.ResultData;
import com.erkobridee.restful.bookmarks.springrest.rest.resource.ResultMessage;

@Controller 
@RequestMapping("/bookmarks")
public class BookmarkRESTController {

	// --------------------------------------------------------------------------
	
	private Logger log = LoggerFactory.getLogger(BookmarkRESTController.class);
	
	// --------------------------------------------------------------------------

	@Autowired
	private IBookmarkDAO dao;

	// --------------------------------------------------------------------------

	@RequestMapping(value = "/search/{find}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ResultData<List<Bookmark>>> search(
		@PathVariable String find,
		@RequestParam(value="page", required=false, defaultValue="1") int page,
		@RequestParam(value="size", required=false, defaultValue="10") int size
	) {
		log.debug("search: " + find);
		
		ResultData<List<Bookmark>> r = dao.findByName(find, page, size);
		ResponseEntity<ResultData<List<Bookmark>>> response = new ResponseEntity<ResultData<List<Bookmark>>>(r, HttpStatus.OK);
		
		return response;
	}	
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<ResultData<List<Bookmark>>> getList(
		@RequestParam(value="page", required=false, defaultValue="1") int page,
		@RequestParam(value="size", required=false, defaultValue="10") int size
	) {
		log.debug("getList");
		
		ResultData<List<Bookmark>> r = dao.list(page, size); 
		
		HttpHeaders responseHeader = new HttpHeaders();
		
		Set<HttpMethod> allowedMethods = new TreeSet<HttpMethod>();
		allowedMethods.add(HttpMethod.GET);
		allowedMethods.add(HttpMethod.POST);
		
		responseHeader.setAllow(allowedMethods);
		
		ResponseEntity<ResultData<List<Bookmark>>> response = new ResponseEntity<ResultData<List<Bookmark>>>(r, responseHeader, HttpStatus.OK);
		
		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> get(@PathVariable String id) {
		Bookmark bookmark = dao.findById(Long.valueOf(id)); 
		
		if(bookmark != null) {
			log.debug("get: " + id);
			
			HttpHeaders responseHeader = new HttpHeaders();
			
			Set<HttpMethod> allowedMethods = new TreeSet<HttpMethod>();
			allowedMethods.add(HttpMethod.PUT);
			allowedMethods.add(HttpMethod.DELETE);
			
			responseHeader.setAllow(allowedMethods);
			
			ResponseEntity<Bookmark> rBookmark = new ResponseEntity<Bookmark>(bookmark, responseHeader, HttpStatus.OK);
			
			return rBookmark;
		} else {
			ResultMessage resultMessage = 
	                new ResultMessage(404, "id: " + id + " not found.");
			
	        return new ResponseEntity<ResultMessage>(
	                resultMessage, HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Bookmark> create(@RequestBody Bookmark value) {
		
		log.debug("insert");
		Bookmark bookmark = dao.save(value); 
		
		HttpHeaders responseHeader = new HttpHeaders(); 
		
		try {
			responseHeader.setLocation(new URI("/bookmarks/" + bookmark.getId()));
		} catch(URISyntaxException e) {
			log.error("Location URI Exception", e);
		}
		
		Set<HttpMethod> allowedMethods = new TreeSet<HttpMethod>();
		allowedMethods.add(HttpMethod.POST);
		allowedMethods.add(HttpMethod.GET);
		
		responseHeader.setAllow(allowedMethods);
		
		ResponseEntity<Bookmark> response = new ResponseEntity<Bookmark>(bookmark, responseHeader, HttpStatus.CREATED);
		
		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<Bookmark> update(@RequestBody Bookmark value) {
		log.debug("update");
		
		Bookmark bookmark = dao.save(value);
		ResponseEntity<Bookmark> response = new ResponseEntity<Bookmark>(bookmark, HttpStatus.ACCEPTED);
		
		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<ResultMessage> delete(@PathVariable String id) {		
		boolean flag = dao.remove(Long.valueOf(id));
		log.debug("remove: " + id + " | status: " + flag);
		
		ResultMessage message; 
		ResponseEntity<ResultMessage> response;		
		
		if(flag) {
			message = new ResultMessage(202, "id: " + id + " removed.");
			response = new ResponseEntity<ResultMessage>(message, HttpStatus.ACCEPTED);
		} else {
			message = new ResultMessage(404, "id: " + id + " not found.");
			response = new ResponseEntity<ResultMessage>(message, HttpStatus.NOT_FOUND);
		}
		
		return response;
	}

}

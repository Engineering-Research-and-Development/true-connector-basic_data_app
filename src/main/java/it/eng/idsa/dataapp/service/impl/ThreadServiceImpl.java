package it.eng.idsa.dataapp.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import it.eng.idsa.dataapp.service.ThreadService;

@Service
public class ThreadServiceImpl implements ThreadService {

	private static final ThreadLocal<Map<String, Object>> threadLocalVariable = new ThreadLocal<>();

	@Override
	public void setThreadLocalValue(String key, Object value) {
		Map<String, Object> map = threadLocalVariable.get();
		if (map == null) {
			map = new HashMap<>();
			threadLocalVariable.set(map);
		}
		map.put(key, value);
	}

	@Override
	public Object getThreadLocalValue(String key) {
		Map<String, Object> map = threadLocalVariable.get();
		if (map == null) {
			return null;
		}
		return map.get(key);
	}

	@Override
	public void removeThreadLocalValue(String key) {
		Map<String, Object> map = threadLocalVariable.get();
		if (map != null) {
			map.remove(key);
		}
	}

}

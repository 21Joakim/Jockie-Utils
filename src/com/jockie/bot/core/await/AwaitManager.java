package com.jockie.bot.core.await;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.core.events.Event;

public class AwaitManager {
	
	private static List<AwaitEvent<?>> awaits = new ArrayList<>();
	
	public static void addAwait(AwaitEvent<?> await) {
		AwaitManager.awaits.add(await);
		
		await.onTimeoutFinish(new Runnable() {
			public void run() {
				AwaitManager.removeAwait(await);
			}
		});
		
		await.startTimeout();
	}
	
	public static void removeAwait(AwaitEvent<?> await) {
		await.stopTimeout();
		
		AwaitManager.awaits.remove(await);
	}
	
	public static void handleAwait(Event event) {
		for(int i = 0; i < AwaitManager.awaits.size(); i++) {
			AwaitManager.awaits.get(i).call(event);
		}
	}
}
<h1>This is a beta library</h1>
All suggestions are welcome and feel free to contribute
</br>
<b>Be aware, this project does lack examples of all the features avaliable</b>

<h2>Libraries:</h2>
https://github.com/DV8FromTheWorld/JDA (The Discord wrapper which the library is built upon)
</br>
https://github.com/google/gson (Used for the Data implementations) 
</br>
https://github.com/google/guava (Used for simplied adding of commands, adding commands by package)

<h2>Gradle (For dependencies and not the actual project):</h2>

```
dependencies {
    compile 'net.dv8tion:JDA:3+'
    compile 'com.google.guava:guava:25.0-jre'
    implementation 'com.google.code.gson:gson:2.8.4'
}
```

<h2>Command structure:</h2>
All commands need to extend ICommand and the standard implementation of that is CommandImpl. CommandImpl allows arguments to be specified as the parameters of a function named onCommand.

</br>

<b>Allowed parameters:</b>
</br>
Any objects are allowed to be present as the parameters however if the object's class is not registered in the <b>ArgumentFactory</b> it will not generate any default arguments for it and throw an exception therefore you have to specifiy them yourself. There is one exception to this with two objects which can not be arguments, <b>MessageReceivedEvent</b> and <b>CommandEvent</b>. Any amount of MessageReceivedEvent and CommandEvent used in your parameters won't matter they will always refer to the context and nothing else. Both MessageReceivedEvent and CommandEvent are optional so none of them have to be specified in the parameters but most of the time you would want at least one of them.

</br>

The <b>ArgumentFactory</b> has a few already registered classes
</br><i>All primitive data types</i>
</br><i>String and any Enum class</i>
</br><i>User, Member, TextChannel, VoiceChannel, Role and Emote from JDA</i>
</br></br>

To specfiy the command you create a method called onCommand, here's a simple ping command

```
public class CommandPing extends CommandImpl {
	public CommandPing() {
		super("ping");
		
		super.setDescription("Simple ping command");
	}
	
	public void onCommand(MessageReceivedEvent event) {
		event.getChannel().sendMessage(event.getJDA().getPing() + " ms").queue();
	}
}
```

</br>

If you want to specify commands you simple add more parameters, here's a simple decide command which will decide between two commands
```
public class CommandDecide extends CommandImpl {
	
	/* No need to create a new one each time someone uses it */
	private Random random = new Random();

	public CommandDecide() {
		super("decide");
		
		super.setDescription("Give me two sentences and I will choose one of them");
	}
	
	public void onCommand(MessageReceivedEvent event, @Argument(description="statement", acceptQuote=true) String statement, @Argument(description="statement 2", acceptQuote=true) String statement2) {
		event.getChannel().sendMessage("**" + (this.random.nextBoolean() ? statement : statement2) + "**" + " seems more reasonable to me!").queue();
	}
}
```
The Argument annotation is used when you want to give the parameter specific properties, some of the properties include
</br><b>endless</b> - This is used when a String for instance needs to go over spaces, it will basically just take everything that is left,  this therefore has to be used on the last parameter.
</br><b>acceptEmpty</b> - This is for when the argument can accept empty input, most of the time this won't be used.
</br><b>acceptQuote</b> - If you want a String for instance to be endless but not the last argument you can use this parameter to force the user to surround the argument with quotes if it is multiple words
</br><b>nullDefault</b> - This is used if you want an argument to be optional, if the argument was not provided it will be null (There are other ways to do optional arguments too)
</br><b>description</b> - A simple description/name of the parameter so that the user knowns what they are inputing

</br>

There is also a way to specify each argument manually, this way allows for custom arguments to be specified and optional arguments to be defined more in depth
```
public class CommandAvatar extends CommandImpl {

	public CommandAvatar() {
		super("avatar", ArgumentFactory.of(User.class).setDescription("user").setDefaultValue(event -> event.getAuthor()).build());
		
		super.setDescription("Get the avatar of a user");
	}
	
	public void onCommand(MessageReceivedEvent event, User user) {
		event.getChannel().sendMessage(new EmbedBuilder().setImage(user.getAvatarUrl()).build()).queue();
	}
}
```
In this example we use the ArgumentFactory to get a Builder of the type User, then we simply set the default argument to the author (so that if they do not define a user they will get their own avatar, how convenient!) and then build it.

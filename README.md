# This is a beta library
All suggestion and contributions are welcome!\
**NOTE:** This project does lack examples of all the available features.

#### Content

* [Libraries Used](#libraries-used)
* [Installation](#installation)
	* [Gradle](#gradle)
	* [Maven](#maven)
* [Bots using Jockie Utils](#bots-using-jockie-utils)
* [How to use](#how-to-use)
	* [CommandListener](#commandlistener)
	* [CommandStore](#commandstore)
* [Command Structure](#command-structure)
	* [Allowed parameters](#allowed-parameters)
	* [Context](#context)
	* [Options](#options)
	* [Arguments](#arguments)
	* [CommandImpl](#commandimpl)
		* [Commands with multiple implementations](#commands-with-multiple-implementations)
		* [Sub-commands](#sub-commands)
		* [Command annotations](#command-annotations)
	* [Modules](#module)
		* [Events](#events)
* [Command extensions](#command-extensions)
    * [Custom command classes](#custom-command-classes)
    * [Using custom command classes](#using-custom-command-classes)
    * [Implementing custom behaviour](#implementing-custom-behaviour)

## Libraries used
[JDA (The Discord wrapper which the library is built upon)](https://github.com/DV8FromTheWorld/JDA)\
[guava (Used for simplified adding of commands, adding commands by package)](https://github.com/google/guava)

## Download
[![](https://jitpack.io/v/21Joakim/Jockie-Utils.svg)](https://jitpack.io/#21Joakim/Jockie-Utils)

### Gradle
```Gradle
repositories {
	jcenter()
	maven { url 'https://jitpack.io' }
}

dependencies {
	implementation 'com.github.21Joakim:Jockie-Utils:VERSION'
}
```

### Maven
```XML
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>

<dependency>
	<groupId>com.github.21Joakim</groupId>
	<artifactId>Jockie-Utils</artifactId>
	<version>VERSION</version>
</dependency>
```

## Bots using Jockie Utils
[Sx4](https://github.com/sx4-discord-bot/Sx4) - A multipurpose bot with a lot of different commands and even an economy system

## How to use

### CommandListener
----

The **CommandListener** class is the brain of this library, this is where everything happens. The **CommandListener** extends JDA's **EventListener** which means it can be registered as an event listener via JDA's addEventListener method.


**Example Usage**
```Java
public static void main(String[] args) throws Exception {
	CommandListener listener = new CommandListener()
		.addCommandStore(CommandStore.of("com.jockie.bot.commands"))
		.addDeveloper(190551803669118976L)
		.setDefaultPrefixes("!");

	new JDABuilder(AccountType.BOT).setToken(TOKEN)
		.addEventListener(listener)
		.build()
		.awaitReady();
}
```

### CommandStore
----

The **CommandStore** class is where all the commands are stored, you can both add commands manually or add them by package, optionally by sub-packages.

**Example Usages**
```Java
CommandStore store = new CommandStore();
/* Load all commands and modules from a pacakge, optionally include all sub-packages */
store.loadFrom(packagePath, true);

/* Add a command manually */
store.addCommands(new CommandHelp());

/* Add a module manually */
store.addCommands(new ModuleFun());

/* Add the command store to the command listener */
listener.addCommandStore(store);
```

You can also load from a package directly.
```Java
CommandStore store = CommandStore.of(packagePath);
```

## Command structure

All commands need to extend ICommand and the standard implementation of that is CommandImpl. CommandImpl allows arguments to be specified as the parameters of a function named onCommand or on_command.

#### Allowed parameters
----

Any objects are allowed to be present as the parameters however if the object's class is not registered in the **ArgumentFactory** it will not generate any default arguments for it and throw an exception therefore you have to specifiy them yourself. There is one exception to this with **CommandEvent** which can not be used as an argument. Any amount of CommandEvent used in your parameters won't matter they will always refer to the context and nothing else. The CommandEvent is also optional, this means it does not have to be a parameter for the command to work, however most of the time you probably do want it.

* The **ArgumentFactory** has a few already registered classes
  * All primitive data types
  * String and any Enum class
  * User, Member, TextChannel, VoiceChannel, Role and Emote from JDA
  * Optional (These arguments are marked as nullDefault)

#### Context
----

Context variables are variables which have information about the command and its environment, these variables can be specified by having a **@Context** annotation on the parameter.

* The already registered context classes are
	* CommandListener
	* JDA
	* Guild
	* Message
	* User, Member
	* MessageChannel, PrivateChannel and Group
	* ChannelType

**Example**

```Java
public void onCommand(@Context User author) {
	author.openPrivateChannel().queue(channel -> {
		channel.sendMessage("Good morning").queue();
	});
}
```

#### Options
----

Options are a sort of boolean command modifier. Options are defined by having two dashes (**--**) in front of the option name and can be placed anywhere in the command.

**Example**

```Java
public onCommand(CommandEvent event, @Option("greet") boolean greet) {
	event.reply((greet ? "Greetings! " : "") + "How are you today?").queue();
}
```

This example could then be executed like 
*`prefix` `command` **--greet*** which would yield the result
**Greetings! How are you today?**.

#### Arguments
----

* The **@Argument** annotation is used when you want to give the parameter specific properties, some of the properties include
	* **value** - The name of the argument
	* **endless** - This is used when a String for instance needs to go over spaces, it will basically just take everything that is 		left,  this therefore has to be used on the last parameter.
	* **acceptEmpty** - This is for when the argument can accept empty input, most of the time this won't be used.
	* **acceptQuote** - If you want a String for instance to be endless but not the last argument you can use this parameter to 			force the user to surround the argument with quotes if it is multiple words
	* **nullDefault** - This is used if you want an argument to be optional, if the argument was not provided it will be null (There 		are other ways to do optional arguments too)
	* **description** - A simple description/name of the parameter so that the user knowns what they are inputing

#### CommandImpl
----

One way to create commands is by extending the **CommandImpl** class, to define the execution method in these types of commands you create an onCommand method, this method is what will be called when a message matching the command's requirements is sent.

**Examples**

A simple ping command.
```Java
public class CommandPing extends CommandImpl {

	public CommandPing() {
		super("ping");
		
		super.setDescription("Simple ping command");
	}
	
	public void onCommand(CommandEvent event) {
		event.reply(event.getJDA().getPing() + " ms").queue();
	}
}
```

Adding arguments to a command is as simple as adding more parameters, here's a simple decide command which will decide between two statements.
```Java
public class CommandDecide extends CommandImpl {
	
	/* No need to create a new one everytime someone uses it */
	private Random random = new Random();

	public CommandDecide() {
		super("decide");
		
		super.setDescription("Give me two sentences and I will choose one of them");
	}
	
	public void onCommand(CommandEvent event, @Argument("statement") String firstStatement, @Argument("secondStatement") String secondStatement) {
		event.reply("**" + (this.random.nextBoolean() ? firstStatement : secondStatement) + "**" + " seems more reasonable to me!").queue();
	}
}
```

You can also use the **Optional** class to define an optional argument!
```Java
public class CommandAvatar extends CommandImpl {

	public CommandAvatar() {
		super("avatar");
		
		super.setDescription("Get the avatar of a user");
	}
	
	public void onCommand(CommandEvent event, @Argument("user") Optional<User> user) {
		event.reply(new EmbedBuilder().setImage(user.orElse(event.getAuthor()).getAvatarUrl()).build()).queue();
	}
}
```

##### Commands with multiple implementations
----

Command implementations are not limited to a single method or pair of arguments, you can have multiple different onCommand methods in the class, this means that there are more than one way to execute that command.

```Java
public class CommandColour extends CommandImpl {
	
	public CommandColour() {
		super("colour");
	}
	
	private String getColourHex(int colourRaw) {
		String colour = Integer.toHexString(colourRaw);
		colour = colour.substring(2, colour.length());
		
		return "#" + colour;
	}

	public void onCommand(CommandEvent event, @Argument("role") Role role) {
		event.reply(String.format("The colour of the role %s is %s", role.getName(), this.getColourHex(role.getColorRaw()))).queue();
	}
	
	public void onCommand(CommandEvent event, @Argument("member") Member member) {
		event.reply(String.format("The colour of the member %s is %s", member.getEffectiveName(), this.getColourHex(member.getColorRaw()))).queue();
	}
}
```

##### Sub-commands
----

Commands can also have sub-commands. A sub-command's trigger will be its parent's trigger with a space separating it from its own trigger, for instance if the parent command is `create` and the sub-command is `role` the trigger would be `create role`, any alias which the parent command `create` has would also work for the sub-command.

Sub-commands can be created by either adding a new method (in a Command class) which has the **@Command** annotation or have a nested class which extends **CommandImpl**, both of these will automatically be detected as a sub-command.

```Java
public class CommandChannel extends CommandImpl {

	public CommandChannel() {
		super("channel");
	}
	
	@Command
	@AuthorPermissions(Permission.MANAGE_CHANNEL)
	@BotPermissions(Permission.MANAGE_CHANNEL)
	public void delete(CommandEvent event, @Argument("channel") TextChannel channel) {
		channel.delete().queue($ -> {
			event.reply(String.format("**%s** has been deleted", channel.getName())).queue();
		});
	}
	
	@Command
	@AuthorPermissions(Permission.MANAGE_CHANNEL)
	@BotPermissions(Permission.MANAGE_CHANNEL)
	public void create(CommandEvent event, @Argument("name") String name) {
		event.getGuild().getController().createTextChannel(name).queue(channel -> {
			event.reply(String.format("**%s** has been created", channel.getName())).queue();
		});
	}
}
```

##### Command annotations
----

The **@Command** annotation can be applied to any method (even onCommand), this will mark the method as a command and in the case of onCommand can be used to modify some of the properties.

#### Modules
----

Modules are a collection of command methods, a module class can either implement **IModule**, use the **@Module** annotation or extend the **ModuleImpl** class.

Modules are added the same way commands are, see [CommandStore](#commandstore).

**Example**

```Java
@Module
public class ModuleFun {
	
	private Random random = new Random();
	
	@Command("roll dice")
	public String rollDice() {
		return String.format("You rolled the number **%s**", this.random.nextInt(6) + 1);
	}
	
	@Command("mention random")
	public String mentionRandom(@Context Guild guild) {
		List<Member> members = guild.getMembers();
		
		return members.get(this.random.nextInt(members.size())).getAsMention();
	}
}
```

##### Events
----

Modules have a few events which can be used.

* Events
	* onModuleLoad
	* onCommandLoad
	* Initialize (Not meant as an event but it kinda is)

**Initialize** and **onCommandLoad** are to an extent the same only difference being that an initialize method can be defined by an annotation and you can have as many as you want in the same module. **onModuleLoad** and **onCommandLoad** can only be defined a single time.

**Example**

```Java
@Module
public class ModuleFun {
	
	public void onModuleLoad() {
		System.out.println(String.format("Module %s has loaded", this.getClass().getSimpleName()));
	}
	
	public void onCommandLoad(MethodCommand command) {
		System.out.println(String.format("Command \"%s\" has loaded", command.getCommand()));
	}
	
	@Initialize
	public void rollDice(MethodCommand command) {
		command.setDescription("The best command for rolling a dice!");
	}
}
```

## Command extensions

The command base can be extended to fit your needs, this can be done for both method commands and class based commands.

### Custom command classes
----

Here is an example of what a custom command class could look like, this class allows for a new **donator** property to be set, it also adds a **@Donator** annotation which can be applied to the command.
```Java
public class ExtendedCommand extends CommandImpl {
	
	private boolean donator = false;
	
	public ExtendedCommand(String name) {
		super(name, true);
		
		this.doAnnotations();
	}
	
	public ExtendedCommand(String name, Method method, Object invoker) {
		super(name, method, invoker);
		
		this.doAnnotations();
	}
	
	public boolean isDonator() {
		return this.donator;
	}
	
	public ExtendedCommand setDonator(boolean donator) {
		this.donator = donator;
		
		return this;
	}
	
	private void doAnnotations() {
		if(this.method != null) {
			if(this.method.isAnnotationPresent(Donator.class)) {
				this.donator = this.method.getAnnotation(Donator.class).value();
			}
		}
	}
}
```

### Using custom command classes
----
After you have made your custom command class you want to use it, to use it for class based commands you simply extend the **ExtendedCommand** class instead of **CommandImpl**, for instance
```Java
public class DonatorCommand extends ExtendedCommand {
    
    public DonatorCommand() {
        super("donator command");
        
        super.setDonator(true);
    }
}
```

To register it for method based commands you need to first create a **IMethodCommandFactory** which is used to create the **MethodCommand** instances, like this
```Java
public class ExtendedCommandFactory implements IMethodCommandFactory<ExtendedCommand> {
	
	public ExtendedCommand create(Method method, String name, Object invoker) {
		return new ExtendedCommand(IMethodCommandFactory.getName(name, method), method, invoker);
	}
}
```
after creating the method command factory you need to register it, like this
```Java
MethodCommandFactory.setDefault(new ExtendedCommandFactory());
```

### Implementing custom behaviour
----

You can implement custom behaviour in two ways, one through the class itself via the verify method and the other through a pre-execute check.

**Through the class itself**, this will make the command get filtered out before it is even checked which means the user will not get any help or response from triggering this command.
```Java
public boolean verify(Message message, CommandListener commandListener) {
	if(!super.verify(message, commandListener)) {
		return false;
	}
	
	if(this.donator && !Donators.isDonator(message.getAuthor().getIdLong())) {
		return false;
	}
	
	return true;
}
```

**Through a pre-execute check**, this means that you can add a custom message to it if you so desire.
```Java
CommandListener listener = new CommandListener();
listener.addPreExecuteCheck((event, command) -> {
	if(command instanceof ExtendedCommand) {
		if(((ExtendedCommand) command).isDonator() && !Donators.isDonator(event.getAuthor().getIdLong())) {
			event.reply("This command is for donators only, check out our patreon https://www.patreon.com/Jockie").queue();
			
			return false;
		}
	}
	
	return true;
});
```

package hu.nomindz.devkit.command;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class CommandRegistry {
  private final JavaPlugin plugin;
  private final Map<String, CommandBase> roots = new HashMap<>();

  public CommandRegistry(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void registerRoot(CommandBase root) {
    roots.put(root.name.toLowerCase(), root);
    PluginCommand pc = plugin.getCommand(root.name);
    if (pc == null)
      throw new IllegalStateException("Command not found in plugin.yml: " + root.name);
    Bridge bridge = new Bridge(plugin, root);
    pc.setExecutor(bridge);
    pc.setTabCompleter(bridge);
  }

  private static final class Bridge implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final CommandBase root;

    public Bridge(JavaPlugin p, CommandBase r) {
      plugin = p;
      root = r;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      try {
        Resolution r = resolve(root, args);
        if (r.expectingChild) {
          sendChildren(sender, r.node);
          return true;
        }
        if (!r.node.perm.test(sender)) {
          sender.sendMessage(ChatColor.RED + "No permission (" + r.node.perm.describe() + ").");
          return true;
        }
        if (r.node.playerOnly && !(sender instanceof org.bukkit.entity.Player)) {
          sender.sendMessage(ChatColor.RED + "Players only.");
          return true;
        }

        Map<String, Object> map = new HashMap<>();
        int i = 0;
        for (var p : r.node.params) {
          if (p.type() instanceof MultiParamType<?> multi) {
            int need = multi.arg_count();
            int have = r.rest.size() - i;
            if (need == -1)
              need = have;

            if (have < need) {
              sender.sendMessage(ChatColor.RED + "Missing argument(s) for: " + p.name());
              sendUsage(sender, r.node);
              return true;
            }
            Object val = multi.parse(plugin.getServer(), sender, r.rest, i);
            i += need;
            map.put(p.name(), val);
          } else {
            if (i < r.rest.size()) {
              String raw = r.rest.get(i++);
              Object val = p.type().parse(plugin.getServer(), sender, raw);
              map.put(p.name(), val);
            } else if (p.optional()) {
              map.put(p.name(), p.defaultValue());
            } else {
              sender.sendMessage(ChatColor.RED + "Missing argument: " + p.name());
              sendUsage(sender, r.node);
              return true;
            }
          }
        }
        r.node.exec.run(plugin.getServer(), sender, map);
      } catch (IllegalArgumentException e) {
        sender.sendMessage(ChatColor.RED + e.getMessage());
      } catch (Exception e) {
        plugin.getLogger().severe("Command error: " + e.getMessage());
        e.printStackTrace();
        sender.sendMessage(ChatColor.RED + "An error occurred. See console.");
      }
      return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command Command, String alias, String[] args) {
      try {
        Resolution r = resolve(root, args);
        if (r.expectingChild) {
          return filter(childNames(r.node), last(args));
        }
        int pi = Math.max(0, r.rest.size() - 1);
        if (pi < r.node.params.size()) {
          var p = r.node.params.get(pi);
          return filter(p.suggestions().apply(plugin.getServer(), sender), last(args));
        }
      } catch (Exception ignored) {
      }
      return Collections.emptyList();
    }

    private record Resolution(CommandBase node, List<String> rest, boolean expectingChild) {
    }

    private Resolution resolve(CommandBase node, String[] args) {
      int i = 0;
      while (i < args.length) {
        String tok = args[i].toLowerCase(Locale.ROOT);
        CommandBase next = node.children.get(tok);
        if (next != null) {
          node = next;
          i++;
          continue;
        }
        if (!node.params.isEmpty()) {
          return new Resolution(node, Arrays.asList(args).subList(i, args.length), false);
        }
        return new Resolution(node, Arrays.asList(args).subList(i, args.length), true);
      }
      return new Resolution(node, List.of(), false);
    }

    private void sendUsage(CommandSender s, CommandBase c) {
      StringBuilder sb = new StringBuilder(ChatColor.GRAY + "Usage: /").append(" ").append(c.name);
      for (var p : c.params)
        sb.append(" ").append(p.optional() ? "[" + p.name() + "]" : "<" + p.name() + ">");
      s.sendMessage(sb.toString());
      if (c.help != null && !c.help.isBlank())
        s.sendMessage(ChatColor.GRAY + c.help);
      if (!c.children.isEmpty())
        s.sendMessage(ChatColor.GRAY + "Subcommands are: " + String.join(", ", childNames(c)));
    }

    private List<String> childNames(CommandBase c) {
      LinkedHashSet<String> set = new LinkedHashSet<>();
      c.children.values().forEach(ch -> {
        set.add(ch.name);
        set.addAll(ch.aliases);
      });
      return new ArrayList<>(set);
    }

    private static String last(String[] a) {
      return a.length == 0 ? "" : a[a.length - 1];
    }

    private static List<String> filter(Collection<String> in, String pref) {
      if (pref == null || pref.isEmpty())
        return new ArrayList<>(in);
      String p = pref.toLowerCase(Locale.ROOT);
      List<String> out = new ArrayList<>();
      for (String s : in)
        if (s.toLowerCase(Locale.ROOT).startsWith(p))
          out.add(s);
      return out;
    }

    private void sendChildren(CommandSender sender, CommandBase c) {
      sender.sendMessage(ChatColor.GRAY + "Usage: /" + c.name + " <subcommand>");
      sender.sendMessage(ChatColor.GRAY + "Subcommands are: " + String.join(", ", childNames(c)));
    }
  }

  public void registerAll(CommandProvider provider) {
    for (CommandBase root : provider.provide(plugin))
      registerRoot(root);
  }
}
# EFab
Fabrication

## CraftTweaker Compatibility

CraftTweaker scripts go in `run/scripts/*.zs`.

EFab exposes two recipe entry points:

- `EFabRecipe.addShaped(output, matrix)`
- `EFabRecipe.addShapeless(output, ingredients)`

Both return a chainable builder supporting:

- `.time(ticks)`
- `.tier("tier")`
- `.tiers(["tier_a", "tier_b"])`
- `.fePerTick(fe)`
- `.fluid(<fluid:minecraft:water> * amount)`

Valid tiers are `gearbox`, `steam`, `liquid`, `fe`, `computing`, `mana`,
`upgrade_magic`, `upgrade_armory`, `upgrade_power`, and `upgrade_digital`.

```zenscript
EFabRecipe.addShaped(<item:minecraft:stone_pickaxe>, [
    [<item:minecraft:diamond>],
    [<item:minecraft:gold_ingot>],
    [<item:minecraft:stick>]
])
    .time(50)
    .tier("liquid")
    .fluid(<fluid:minecraft:water> * 500);

EFabRecipe.addShapeless("crt_glowstone_test", <item:minecraft:glowstone_dust>, [
    <item:minecraft:redstone>
])
    .time(100)
    .tier("fe")
    .fePerTick(10);
```

## KubeJS Compatibility

KubeJS server scripts go in `run/kubejs/server_scripts/*.js`.

EFab exposes two KubeJS-style recipe builders:

- `event.recipes.addShaped(output, matrix)`
- `event.recipes.addShapeless(output, ingredients)`

The native schema names are also available as `event.recipes.efab.add_shaped(...)`
and `event.recipes.efab.add_shapeless(...)`. The camelCase functions are
registered as KubeJS mappings for script ergonomics.

Both builders support these chainable methods:

- `.time(ticks)`
- `.tier("tier")`
- `.fePerTick(fe)`
- `.fluid("<amount>x <fluid id>")`
- `.fluid("<fluid id>", amount)`
- `.requirement(jsonOrRequirement)`

For shaped recipes, use a 1x1 to 3x3 matrix. Empty cells can be `''` or `null`.

```js
ServerEvents.recipes(event => {
  event.recipes.addShaped(
    'minecraft:stone_pickaxe',
    [
      ['minecraft:diamond', '', ''],
      ['minecraft:gold_ingot', '', ''],
      ['minecraft:stick', '', '']
    ]
  )
    .time(50)
    .tier('liquid')
    .fluid('500x minecraft:water')
    .id('efab:kubejs_stone_pickaxe_test');

  event.recipes.addShapeless(
    'minecraft:glowstone_dust',
    [
      'minecraft:redstone'
    ]
  )
    .time(100)
    .tier('fe')
    .fePerTick(10)
    .id('efab:kubejs_glowstone_test');
});
```

The in-repo test script is `run/kubejs/server_scripts/efab_test.js`.

### KubeJS Extension API

EFab's KubeJS recipe builders are normal Java builders, following the same
pattern as Custom Machinery. Shared builder operations live in
`mcjty.efab.api.kubejs.RecipeJSBuilder`.

An addon can define a chainable requirement interface by extending
`RecipeJSBuilder` and adding default methods that call `addRequirement(...)`:

```java
package example.compat.efab;

import mcjty.efab.api.kubejs.RecipeJSBuilder;

public interface ManaJS extends RecipeJSBuilder {

    default RecipeJSBuilder mana(int amount) {
        return addRequirement(new ManaRequirement(amount));
    }
}
```

When the addon is external to EFab, that interface still has to be attached to
EFab's KubeJS recipe builder classes by the addon integration layer, for example
with a mixin targeting `EFabGridShapedRecipeJSBuilder` and
`EFabGridShapelessRecipeJSBuilder`.

1.1.0
-----

##### Features

 * Added attribute `app:cv_max_height="${dimension}"` to make the ChipView's content scrollable beyond this limit (fixes #8)
 * Added attribute `app:cv_vertical_spacing="${dimension}"` to allow configurable spacing between rows (fixes #4)
 
##### Misc

 * Added `getChips()` (fixes #6)
 * Added `getEditText()`, e.g. to allow the ChipsView to be made non-editable (fixes #2)
 * Improved the cursor's alignment to be centered towards the Chips in its row
 * Improved the sample application
 
##### Upgrade Notes

As the ChipsView itself is now a ScrollView you may want to remove any ScrollView that you already used to wrap the ChipView's content and use `app:cv_max_height` instead.

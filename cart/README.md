URL: localhost:8083

- **/cart** --> GET, GET current user cart. Needs to be authenticated also,
  check if username passed in authorization == cart username. If null
  returns user not have cart yet
- **/cart/items** --> POST --> only adds, if    exist --> add quantity.
  Also creates new document if it is not exist    in the first place
- **/cart/items/{skuCode}** --> DELETE --> delete item if exist
- **/cart/items/edit** --> PUT --> custom add qty per SKU
- **/cart/items** --> DELETE --> delete cart
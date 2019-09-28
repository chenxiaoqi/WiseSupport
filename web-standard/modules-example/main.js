import greeting from './modules/api.js';
import {sayWorld,sayHello,draw as myDraw} from "./modules/api.js";

import * as Aggregate from './modules/aggregate.js';

import * as Module from './modules/api.js';

greeting();
sayWorld();
sayHello();
myDraw();

Module.sayHello();

Aggregate.sayHello();
Aggregate.inAggregate();

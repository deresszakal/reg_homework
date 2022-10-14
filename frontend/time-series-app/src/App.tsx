import React from 'react';
import { AddGreeting } from './AddGreeting';
import { GreetingView } from './GreetingView';
import { DeleteGreeting } from './DeleteGreeting';

export const App = () => {
  return (
    <div>
      <GreetingView />
      <hr/>
      <AddGreeting />
      <hr/>
      <DeleteGreeting />
    </div>
  );
};

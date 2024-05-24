import {Chat} from "./Chat";

export class Message {
  ms_id?: number;
  chat?: Chat;
  sender?: string;
  t_stamp?: string;
  content?: string;
}
